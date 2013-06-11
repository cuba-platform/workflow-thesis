/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Devyatkin
 * Created: 30.03.2011 17:47:27
 *
 * $Id$
 */
package com.haulmont.workflow.core;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.DesignFile;
import com.haulmont.workflow.core.entity.DesignProcessVariable;
import com.haulmont.workflow.core.entity.DesignScript;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.ExternalizableConverter;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;
import java.util.zip.CRC32;

public class DesignImportExportHelper {
    private static final String DESIGN = "design.xml";
    protected static final String ENCODING = "UTF-8";

    public static byte[] exportDesign(Design design) throws IOException, FileStorageException {
        EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
        em.setView(AppBeans.get(Metadata.class).getViewRepository().getView(design.getClass(), "export"));
        design = em.find(design.getClass(), design.getId());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(byteArrayOutputStream);
        zipOutputStream.setMethod(ZipArchiveOutputStream.STORED);
        zipOutputStream.setEncoding(ENCODING);
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

    public static byte[] exportDesigns(Collection<Design> designs) throws IOException, FileStorageException {
        Transaction tx = AppBeans.get(Persistence.class).createTransaction();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(byteArrayOutputStream);
            zipOutputStream.setMethod(ZipArchiveOutputStream.STORED);
            zipOutputStream.setEncoding(ENCODING);
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

    private static String replaceForbiddenCharacters(String fileName) {
        return fileName.replaceAll("[\\,/,:,\\*,\",<,>,\\|]", "");
    }

    private static ArchiveEntry newStoredEntry(String name, byte[] data) {
        ZipArchiveEntry zipEntry = new ZipArchiveEntry(name);
        zipEntry.setSize(data.length);
        zipEntry.setCompressedSize(zipEntry.getSize());
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        zipEntry.setCrc(crc32.getValue());
        return zipEntry;
    }

    private static String toXML(Object o) {
        XStream xStream = createXStream();
        return xStream.toXML(o);
    }

    private static <T> T fromXML(Class clazz, String xml) {
        XStream xStream = createXStream();
        Object o = xStream.fromXML(xml);
        return (T) o;
    }

    private static XStream createXStream() {
        XStream xStream = new XStream();
        xStream.getConverterRegistry().removeConverter(ExternalizableConverter.class);
        return xStream;
    }

    private static Design findExistsDesign(Design design) {
        EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
        Boolean softDeleteion = em.isSoftDeletion();
        em.setSoftDeletion(false);
        em.setView(AppBeans.get(Metadata.class).getViewRepository().getView(Design.class, "remove"));
        Design existsDesign = em.find(design.getClass(), design.getId());
        em.setView(null);
        em.setSoftDeletion(softDeleteion);
        return existsDesign;
    }

    public static Collection<Design> importDesigns(byte[] zipBytes) throws IOException, FileStorageException {
        LinkedList<Design> designs = null;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(zipBytes);
        ZipArchiveInputStream archiveReader;
        archiveReader = new ZipArchiveInputStream(byteArrayInputStream);
        ZipArchiveEntry nextZipEntry = null;
        while ((nextZipEntry = archiveReader.getNextZipEntry()) != null) {
            if (designs == null) {
                designs = new LinkedList<Design>();
            }
            final byte[] buffer = readBytesFromEntry(archiveReader);
            Design design = importDesign(buffer, !nextZipEntry.getName().equals(DESIGN));
            designs.add(design);
        }
        byteArrayInputStream.close();
        return designs;
    }

    private static byte[] readBytesFromEntry(ZipArchiveInputStream archiveReader) throws IOException {
        return IOUtils.toByteArray(archiveReader);
    }

    public static Design importDesign(byte[] zipBytes, Boolean isArchive) throws IOException, FileStorageException {
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

        Transaction tx = AppBeans.get(Persistence.class).createTransaction();
        try {
            Design existsDesign = findExistsDesign(design);

            if (existsDesign != null) {
                cleanExistDesign(existsDesign);
                copyFields(design, existsDesign);
                existsDesign.setScripts(design.getScripts());
                existsDesign.setDesignProcessVariables(design.getDesignProcessVariables());
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

    private static void copyFields(Design design, Design existsDesign) {
        MetaClass caseMetaClass = AppBeans.get(Metadata.class).getSession().getClass(design.getClass());
        Collection<MetaProperty> metaProperties = caseMetaClass.getProperties();

        for (MetaProperty metaProperty : metaProperties) {
            final String propertyName = metaProperty.getName();
            if ((MetaProperty.Type.DATATYPE.equals(metaProperty.getType()) || MetaProperty.Type.ENUM.equals(metaProperty.getType()))
                    && !metaProperty.isReadOnly()) {
                Object value = design.getValue(propertyName);
                existsDesign.setValue(propertyName, value);
            }
        }
    }

    private static Design persistDesign(Design design) {
        EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
        if (PersistenceHelper.isNew(design)) {
            em.persist(design);
        } else {
            design = em.merge(design);
        }
        for (DesignScript script : design.getScripts()) {
            em.persist(script);
        }
        for (DesignProcessVariable variable : design.getDesignProcessVariables()) {
            em.persist(variable);
        }
        return design;
    }


    private static void cleanExistDesign(Design existsDesign) {
        EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
        for (DesignFile file : existsDesign.getDesignFiles()) {
            em.remove(file);
        }
        for (DesignScript script : existsDesign.getScripts()) {
            em.remove(script);
        }
        for (DesignProcessVariable variable : existsDesign.getDesignProcessVariables()) {
            em.remove(variable);
        }
        for (DesignFile designFile : existsDesign.getDesignFiles()) {
            em.remove(designFile);
        }
        existsDesign.setDeleteTs(null);
        existsDesign.setDeletedBy(null);
    }

    private static Design changeAttributes(Design design) {
        User user = AppBeans.get(UserSessionSource.class).getUserSession().getUser();
        design.setCreatedBy(user.getName());
        design.setCompileTs(null);
        design.setCreateTs(new Date());
        design.setUpdatedBy(null);
        design.setUpdateTs(null);

        for (DesignScript script : design.getScripts()) {
            script.setUuid(UUID.randomUUID());
            script.setCreateTs(new Date());
            script.setCreatedBy(user.getName());
            script.setUpdatedBy(null);
            script.setUpdateTs(null);
        }

        for (DesignProcessVariable variable : design.getDesignProcessVariables()) {
            variable.setUuid(UUID.randomUUID());
            variable.setCreateTs(new Date());
            variable.setCreatedBy(user.getName());
            variable.setUpdatedBy(null);
            variable.setUpdateTs(null);
        }
        return design;
    }
}
