package br.com.oriontask.backend.service;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({TasksServiceChangeStatusTest.class, TasksServiceUpdateTaskTest.class})
public class TasksServiceTestSuite {}
