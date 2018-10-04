package com.davidhm.pqtm.mybooks;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Test instrumentado, que se ejecutará en un dispositivo Android.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Contexto de la aplicación bajo test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.davidhm.pqtm.mybooks", appContext.getPackageName());
    }
}
