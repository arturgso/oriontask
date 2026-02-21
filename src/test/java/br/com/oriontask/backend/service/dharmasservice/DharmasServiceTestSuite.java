package br.com.oriontask.backend.service.dharmasservice;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
  DharmasServiceGetByUserTest.class,
  DharmasServiceCreateTest.class,
  DharmasServiceUpdateTest.class,
  DharmasServiceDeleteTest.class,
  DharmasServiceToggleHiddenTest.class
})
public class DharmasServiceTestSuite {}
