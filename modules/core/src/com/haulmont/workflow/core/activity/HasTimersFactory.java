package com.haulmont.workflow.core.activity;

import com.haulmont.workflow.core.timer.AssignmentTimersFactory;

/**
 * @author sevostyanov
 * @version $Id$
 */
public interface HasTimersFactory {
    AssignmentTimersFactory getTimersFactory();
}
