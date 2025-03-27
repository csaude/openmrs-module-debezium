package org.openmrs.module.debezium;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Test;
import org.openmrs.ConceptName;
import org.openmrs.Person;
import org.openmrs.api.impl.ConceptServiceImpl;
import org.openmrs.module.debezium.utils.Utils;

public class UtilsTest {
	
	@Test
	public void getFieldValue_shouldGetTheFieldValue() throws Exception {
		Person person = new Person();
		Field field = Person.class.getDeclaredField("gender");
		assertFalse(field.canAccess(person));
		final String gender = "M";
		Person msg = new Person();
		msg.setGender(gender);
		assertEquals(gender, Utils.getFieldValue(msg, field));
		assertFalse(field.canAccess(person));
	}
	
	@Test
	public void setFieldValue_shouldSetTheFieldValue() throws Exception {
		Person person = new Person();
		Field field = Person.class.getDeclaredField("gender");
		assertFalse(field.canAccess(person));
		final String gender = "M";
		assertNull(person.getGender());
		Utils.setFieldValue(person, field, gender);
		assertEquals(gender, person.getGender());
		assertFalse(field.canAccess(person));
	}
	
	@Test
	public void invokeMethod_shouldInvokeTheMethod() throws Exception {
		ConceptServiceImpl service = new ConceptServiceImpl();
		Method method = ConceptServiceImpl.class.getDeclaredMethod("cloneConceptName", ConceptName.class);
		assertFalse(method.canAccess(service));
		
		ConceptName c = (ConceptName) Utils.invokeMethod(service, method, new ConceptName());
		
		assertNotNull(c);
		assertFalse(method.canAccess(service));
	}
	
}
