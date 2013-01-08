package com.xtremelabs.robolectric;

import com.xtremelabs.robolectric.bytecode.Setup;
import javassist.CtClass;
import javassist.NotFoundException;
import org.junit.runners.model.InitializationError;

import java.lang.reflect.Method;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;

public class TestRunners {
    public static class WithCustomClassList extends RobolectricTestRunner {
        public WithCustomClassList(Class<?> testClass) throws InitializationError {
            super(testClass);
        }

        @Override
        public RobolectricContext createRobolectricContext() {
            return new RobolectricContext() {
                @Override
                protected RobolectricConfig createRobolectricConfig() {
                    return new RobolectricConfig(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets"));
                }

                @Override
                public Setup createSetup() {
                    return new Setup() {
                        @Override
                        public boolean shouldInstrument(CtClass ctClass) throws NotFoundException {
                            String name = ctClass.getName();
                            if (name.equals("com.xtremelabs.robolectric.bytecode.AndroidTranslatorClassInstrumentedTest$CustomPaint")
                                    || name.equals("com.xtremelabs.robolectric.bytecode.AndroidTranslatorClassInstrumentedTest$ClassWithPrivateConstructor")) {
                                return true;
                            }
                            return super.shouldInstrument(ctClass);
                        }
                    };
                }
            };
        }
    }

    public static class WithoutDefaults extends RobolectricTestRunner {
        public WithoutDefaults(Class<?> testClass) throws InitializationError {
            super(testClass);
        }

        @Override
        public RobolectricContext createRobolectricContext() {
            return new RobolectricContext();
        }

        @Override protected void configureShadows(Method testMethod) {
            // Don't do any class binding, because that's what we're trying to test here.
        }

        @Override
        protected Class<? extends Delegate> getDelegateClass() {
            return MyDelegate.class;
        }

        public static class MyDelegate extends Delegate {
            @Override
            public void setupApplicationState(Method testMethod) {
                // Don't do any resource loading or app init, because that's what we're trying to test here.
            }
        }
    }

    public static class WithDefaults extends RobolectricTestRunner {
        public WithDefaults(Class<?> testClass) throws InitializationError {
            super(testClass);
        }

        @Override
        public RobolectricContext createRobolectricContext() {
            return new RobolectricContext() {
                @Override
                protected RobolectricConfig createRobolectricConfig() {
                    return new RobolectricConfig(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets"));
                }
            };
        }
    }

    public static class RealApisWithDefaults extends RobolectricTestRunner {
        public RealApisWithDefaults(Class<?> testClass) throws InitializationError {
            super(testClass);
        }

        @Override
        public RobolectricContext createRobolectricContext() {
            return new RobolectricContext() {
                @Override
                protected RobolectricConfig createRobolectricConfig() {
                    return new RobolectricConfig(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets"));
                }

                @Override
                public Setup createSetup() {
                    return new Setup() {
                        @Override
                        public boolean invokeApiMethodBodiesWhenShadowMethodIsMissing(Class clazz) {
                            return true;
                        }
                    };
                }
            };
        }
    }

    public static class RealApisWithoutDefaults extends RobolectricTestRunner {
        public RealApisWithoutDefaults(Class<?> testClass) throws InitializationError {
            super(testClass);
        }

        @Override
        public RobolectricContext createRobolectricContext() {
            return new RobolectricContext() {
                @Override
                public Setup createSetup() {
                    return new Setup() {
                        @Override
                        public boolean invokeApiMethodBodiesWhenShadowMethodIsMissing(Class clazz) {
                            return true;
                        }
                    };
                }
            };
        }

        @Override protected void configureShadows(Method testMethod) {
            // Don't do any class binding, because that's what we're trying to test here.
        }

        @Override
        protected Class<? extends Delegate> getDelegateClass() {
            return MyDelegate.class;
        }

        public static class MyDelegate extends Delegate {
            @Override
            public void setupApplicationState(Method testMethod) {
                // Don't do any resource loading or app init, because that's what we're trying to test here.
            }
        }
    }
}
