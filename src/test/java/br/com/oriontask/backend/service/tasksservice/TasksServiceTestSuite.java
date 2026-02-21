package br.com.oriontask.backend.service.tasksservice;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
  TasksServiceChangeStatusTest.class,
  TasksServiceUpdateTaskTest.class,
  TasksServiceMoveToNowTest.class,
  TasksServiceMarkAsDoneTest.class,
  TasksServiceSnoozeTaskTest.class
})
public class TasksServiceTestSuite {}
