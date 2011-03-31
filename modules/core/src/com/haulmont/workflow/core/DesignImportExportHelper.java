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

import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Design;
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
import java.util.Date;
import java.util.UUID;
import java.util.zip.CRC32;

public class DesignImportExportHelper {
    private static final String DESIGN = "design.xml";
    protected static final String ENCODING = "UTF-8";

    public static byte[] exportDesign(Design design) throws IOException, FileStorageException {
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

    public static Design importDesign(byte[] zipBytes) throws IOException, FileStorageException {
        Design design = null;
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

        changeAttributes(design);

        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            if (PersistenceHelper.isNew(design)) {
                em.persist(design);
                for (DesignScript script : design.getScripts()) {
                    em.persist(script);
                }
            } else {
                em.merge(design);
            }
            tx.commit();
        } finally {
            tx.end();
        }
        return design;
    }

    private static Design changeAttributes(Design design) {
        User user = SecurityProvider.currentUserSession().getUser();
        design.setCreatedBy(user.getName());
        design.setUuid(UUID.randomUUID());
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
        return design;
    }
}
