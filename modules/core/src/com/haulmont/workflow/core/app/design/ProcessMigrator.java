/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.app.design;

import java.util.UUID;

public interface ProcessMigrator {

    Result checkMigrationPossibility(UUID designId, UUID procId);

    void migrate(UUID designId, UUID procId, String oldJbpmProcessKey);

    class Result {
        private boolean success;
        private String message;
        private String oldJbpmProcessKey;

        public Result(boolean success, String message) {
            this(success, message, null);
        }

        public Result(boolean success, String message, String oldJbpmProcessKey) {
            this.success = success;
            this.message = message;
            this.oldJbpmProcessKey = oldJbpmProcessKey;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getOldJbpmProcessKey() {
            return oldJbpmProcessKey;
        }
    }
}