/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app.design;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.entity.BaseEntityInternalAccess;
import com.haulmont.cuba.core.entity.BaseGenericIdEntity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.app.CompilationMessage;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.DesignFile;
import com.haulmont.workflow.core.entity.DesignProcessVariable;
import com.haulmont.workflow.core.entity.DesignScript;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import com.haulmont.workflow.core.exception.DesignDeploymentException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.ExternalizableConverter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Collections;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.Transient;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;
import java.util.zip.CRC32;

@Component(DesignerWorkerAPI.NAME)
public class DesignerWorker implements DesignerWorkerAPI {

    @Inject
    protected DesignCompiler compiler;

    @Inject
    protected DesignDeployer deployer;

    @Inject
    protected ProcessMigrator migrator;

    @Inject
    protected Persistence persistence;

    @Inject
    protected Metadata metadata;

    @Inject
    protected ViewRepository viewRepository;

    @Inject
    protected UserSessionSource userSessionSource;

    protected static final String DESIGN = "design.xml";

    @Override
    public CompilationMessage compileDesign(UUID designId) throws DesignCompilationException {
        return compiler.compileDesign(designId);
    }

    @Override
    public void deployDesign(UUID designId, UUID procId, Role role) throws DesignDeploymentException {
        ProcessMigrator.Result result = null;
        if (procId != null) {
            result = migrator.checkMigrationPossibility(designId, procId);
            if (!result.isSuccess())
                throw new DesignDeploymentException(result.getMessage());
        }

        deployer.deployDesign(designId, procId, role);

        if (result != null && result.getOldJbpmProcessKey() != null) {
            migrator.migrate(designId, procId, result.getOldJbpmProcessKey());
        }
    }

    protected ArchiveEntry newStoredEntry(String name, byte[] data) {
        ZipArchiveEntry zipEntry = new ZipArchiveEntry(name);
        zipEntry.setSize(data.length);
        zipEntry.setCompressedSize(zipEntry.getSize());
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        zipEntry.setCrc(crc32.getValue());
        return zipEntry;
    }

    protected String replaceForbiddenCharacters(String fileName) {
        return fileName.replaceAll("[\\,/,:,\\*,\",<,>,\\|]", "");
    }

    @Override
    public byte[] exportDesigns(Collection<Design> designs) throws IOException, FileStorageException {
        Transaction tx = persistence.createTransaction();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(byteArrayOutputStream);
            zipOutputStream.setMethod(ZipArchiveOutputStream.STORED);
            zipOutputStream.setEncoding(StandardCharsets.UTF_8.name());
            for (Design design : designs) {
                try {
                    byte[] designBytes = exportDesign(design);
                    ArchiveEntry singleDesignEntry = newStoredEntry(replaceForbiddenCharacters(design.getName()) + ".zip", designBytes);
                    zipOutputStream.putArchiveEntry(singleDesignEntry);
                    zipOutputStream.write(designBytes);
                    zipOutputStream.closeArchiveEntry();
                } catch (Exception ex) {
                    throw new RuntimeException("Exception occured while exporting design\"" + design.getName() + "\".", ex);
                }
            }
            zipOutputStream.close();
            tx.commit();
            return byteArrayOutputStream.toByteArray();
        } finally {
            tx.end();
        }
    }

    @Override
    public byte[] exportDesign(Design design) throws IOException, FileStorageException {
        EntityManager em = persistence.getEntityManager();
        design = em.find(design.getClass(), design.getId(), "export");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(byteArrayOutputStream);
        zipOutputStream.setMethod(ZipArchiveOutputStream.STORED);
        zipOutputStream.setEncoding(StandardCharsets.UTF_8.name());
        String xml = toXML(design);
        byte[] xmlBytes = xml.getBytes();
        ArchiveEntry zipEntryDesign = newStoredEntry(DESIGN, xmlBytes);
        zipOutputStream.putArchiveEntry(zipEntryDesign);
        zipOutputStream.write(xmlBytes);
        try {
            zipOutputStream.closeArchiveEntry();
        } catch (Exception ex) {
            throw new RuntimeException("Exception occured while exporting design\"" + design.getName() + "\".", ex);
        }

        zipOutputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public Design importDesign(byte[] bytes) throws IOException, FileStorageException {
        return importDesignsCollection(bytes).iterator().next();
    }

    @Override
    public Design importDesign(byte[] zipBytes, Boolean isArchive) throws IOException, FileStorageException {
        Design design = null;
        if (isArchive) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipBytes);
            ZipArchiveInputStream archiveReader;
            archiveReader = new ZipArchiveInputStream(byteArrayInputStream);
            ZipArchiveEntry archiveEntry;

            while ((archiveEntry = archiveReader.getNextZipEntry()) != null) {
                if (archiveEntry.getName().equals(DESIGN)) {
                    String xml = new String(IOUtils.toByteArray(archiveReader));
                    design = fromXML(Design.class, xml);
                    break;
                }
            }
            byteArrayInputStream.close();
        } else {
            String xml = new String(zipBytes);
            design = fromXML(Design.class, xml);
        }

        Transaction tx = persistence.createTransaction();
        try {
            Design existsDesign = findExistsDesign(design);

            if (existsDesign != null) {
                cleanExistDesign(existsDesign);
                copyFields(design, existsDesign);
                existsDesign.setScripts(design.getScripts());
                for (DesignScript script : existsDesign.getScripts()) {
                    script.setDesign(existsDesign);
                }
                if (design.getDesignProcessVariables() != null) {
                    existsDesign.setDesignProcessVariables(design.getDesignProcessVariables());
                } else {
                    existsDesign.setDesignProcessVariables(Collections.emptySet());
                }
                for (DesignProcessVariable variable : existsDesign.getDesignProcessVariables()) {
                    variable.setDesign(existsDesign);
                }
                design = existsDesign;
            }
            changeAttributes(design);
            design = persistDesign(design);
            tx.commit();
            return design;
        } finally {
            tx.end();
        }
    }

    @Override
    public Collection<Design> importDesigns(byte[] zipBytes) throws IOException, FileStorageException {
        return importDesignsCollection(zipBytes);
    }

    protected Collection<Design> importDesignsCollection(byte[] zipBytes)
            throws IOException, FileStorageException {
        LinkedList<Design> designs = null;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipBytes);
        ZipArchiveInputStream archiveReader;
        archiveReader = new ZipArchiveInputStream(byteArrayInputStream);
        ZipArchiveEntry nextZipEntry;
        while ((nextZipEntry = archiveReader.getNextZipEntry()) != null) {
            if (designs == null) {
                designs = new LinkedList<>();
            }
            final byte[] buffer = readBytesFromEntry(archiveReader);
            Design design = importDesign(buffer, !nextZipEntry.getName().equals(DESIGN));
            designs.add(design);
        }
        byteArrayInputStream.close();
        return designs;
    }

    protected String toXML(Object o) {
        XStream xStream = createXStream();
        excludeTransientFields(o.getClass(), xStream);
        return xStream.toXML(o);
    }

    protected void excludeTransientFields(Class clazz, XStream xStream) {
        for (Field field : clazz.getDeclaredFields()) {
            Transient annotation = field.getAnnotation(Transient.class);
            if (annotation != null)
                xStream.omitField(clazz, field.getName());
        }
        if (clazz.getSuperclass() != null) {
            excludeTransientFields(clazz.getSuperclass(), xStream);
        }
    }

    protected <T> T fromXML(Class clazz, String xml) {
        XStream xStream = createXStream();
        Object o = xStream.fromXML(xml);
        if (o instanceof BaseGenericIdEntity) {
            BaseEntityInternalAccess.setNew((BaseGenericIdEntity) o, true);
        }
        return (T) o;
    }

    protected XStream createXStream() {
        XStream xStream = new XStream();
        xStream.getConverterRegistry().removeConverter(ExternalizableConverter.class);
        return xStream;
    }

    protected byte[] readBytesFromEntry(ZipArchiveInputStream archiveReader) throws IOException {
        return IOUtils.toByteArray(archiveReader);
    }

    protected Design findExistsDesign(Design design) {
        EntityManager em = persistence.getEntityManager();
        Boolean softDeleteion = em.isSoftDeletion();
        em.setSoftDeletion(false);
        Design existsDesign = em.find(design.getClass(), design.getId(), "remove");
        em.setSoftDeletion(softDeleteion);
        return existsDesign;
    }

    protected void copyFields(Design design, Design existsDesign) {
        MetaClass caseMetaClass = metadata.getSession().getClass(design.getClass());
        Collection<MetaProperty> metaProperties = caseMetaClass.getProperties();

        for (MetaProperty metaProperty : metaProperties) {
            final String propertyName = metaProperty.getName();
            if ((MetaProperty.Type.DATATYPE.equals(metaProperty.getType()) || MetaProperty.Type.ENUM.equals(metaProperty.getType()))
                    && !metaProperty.isReadOnly() && !"version".equals(metaProperty.getName())) {
                Object value = design.getValue(propertyName);
                existsDesign.setValue(propertyName, value);
            }
        }
    }

    protected Design persistDesign(Design design) {
        EntityManager em = persistence.getEntityManager();
        if (PersistenceHelper.isNew(design)) {
            em.persist(design);
        } else {
            design = em.merge(design);
        }
        for (DesignScript script : design.getScripts()) {
            em.persist(script);
        }
        if (design.getDesignProcessVariables() != null) {
            if (CollectionUtils.isNotEmpty(design.getDesignProcessVariables()))
                for (DesignProcessVariable variable : design.getDesignProcessVariables()) {
                    em.persist(variable);
                }
        }
        return design;
    }

    protected void cleanExistDesign(Design existsDesign) {
        EntityManager em = persistence.getEntityManager();
        for (DesignFile file : existsDesign.getDesignFiles()) {
            em.remove(file);
        }
        for (DesignScript script : existsDesign.getScripts()) {
            em.remove(script);
        }
        if (existsDesign.getDesignProcessVariables() != null) {
            for (DesignProcessVariable variable : existsDesign.getDesignProcessVariables()) {
                em.remove(variable);
            }
        }
        for (DesignFile designFile : existsDesign.getDesignFiles()) {
            em.remove(designFile);
        }
        existsDesign.setDeleteTs(null);
        existsDesign.setDeletedBy(null);
    }

    protected Design changeAttributes(Design design) {
        User user = userSessionSource.getUserSession().getUser();
        design.setCreatedBy(user.getName());
        design.setCompileTs(null);
        design.setCreateTs(new Date());
        design.setUpdatedBy(null);
        design.setUpdateTs(null);

        for (DesignScript script : design.getScripts()) {
            script.setId(UUID.randomUUID());
            script.setCreateTs(new Date());
            script.setCreatedBy(user.getName());
            script.setUpdatedBy(null);
            script.setUpdateTs(null);
        }

        if (CollectionUtils.isNotEmpty(design.getDesignProcessVariables()))
            for (DesignProcessVariable variable : design.getDesignProcessVariables()) {
                variable.setId(UUID.randomUUID());
                variable.setCreateTs(new Date());
                variable.setCreatedBy(user.getName());
                variable.setUpdatedBy(null);
                variable.setUpdateTs(null);
            }
        return design;
    }
}