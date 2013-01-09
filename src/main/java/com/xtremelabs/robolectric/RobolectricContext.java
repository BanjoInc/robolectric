package com.xtremelabs.robolectric;

import com.xtremelabs.robolectric.bytecode.AndroidTranslator;
import com.xtremelabs.robolectric.bytecode.AsmInstrumentingClassLoader;
import com.xtremelabs.robolectric.bytecode.ClassCache;
import com.xtremelabs.robolectric.bytecode.ClassHandler;
import com.xtremelabs.robolectric.bytecode.RobolectricInternals;
import com.xtremelabs.robolectric.bytecode.Setup;
import com.xtremelabs.robolectric.bytecode.ShadowWrangler;
import com.xtremelabs.robolectric.bytecode.ZipClassCache;
import com.xtremelabs.robolectric.internal.RobolectricTestRunnerInterface;
import com.xtremelabs.robolectric.res.ResourceLoader;
import com.xtremelabs.robolectric.res.ResourcePath;
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.jetbrains.annotations.Nullable;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.resolution.ArtifactResult;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.xtremelabs.robolectric.RobolectricTestRunner.isBootstrapped;

public class RobolectricContext {
    private static final Map<Class<? extends RobolectricTestRunner>, RobolectricContext> contextsByTestRunner = new HashMap<Class<? extends RobolectricTestRunner>, RobolectricContext>();

    private final AndroidManifest appManifest;
    private final List<AndroidManifest> libraryManifests;
    private final ClassLoader robolectricClassLoader;
    private final ClassHandler classHandler;
    private static RepositorySystem repositorySystem;
    public static RobolectricContext mostRecentRobolectricContext; // ick, race condition

    public interface Factory {
        RobolectricContext create();
    }

    public static Class<?> bootstrap(Class<? extends RobolectricTestRunner> robolectricTestRunnerClass, Class<?> testClass, Factory factory) {
        if (isBootstrapped(robolectricTestRunnerClass) || isBootstrapped(testClass)) {
            if (!isBootstrapped(testClass)) throw new IllegalStateException("test class is somehow not bootstrapped");
            return testClass;
        }

        RobolectricContext robolectricContext;
        synchronized (contextsByTestRunner) {
            robolectricContext = contextsByTestRunner.get(robolectricTestRunnerClass);
            if (robolectricContext == null) {
                robolectricContext = factory.create();
                contextsByTestRunner.put(robolectricTestRunnerClass, robolectricContext);
            }
        }

        mostRecentRobolectricContext = robolectricContext;

        return robolectricContext.bootstrapTestClass(testClass);
    }

    public RobolectricContext() {
        ClassCache classCache = createClassCache();
        Setup setup = createSetup();
        classHandler = createClassHandler(setup);
        appManifest = createAppManifest();
        libraryManifests = createLibraryManifests(appManifest);
        AndroidTranslator androidTranslator = createAndroidTranslator(setup, classCache);
        robolectricClassLoader = createRobolectricClassLoader(setup, classCache, androidTranslator);
    }

    private ClassHandler createClassHandler(Setup setup) {
        return new ShadowWrangler(setup);
    }

    public ClassCache createClassCache() {
        final String classCachePath = System.getProperty("cached.robolectric.classes.path");
        final File classCacheDirectory;
        if (null == classCachePath || "".equals(classCachePath.trim())) {
            classCacheDirectory = new File("./tmp");
        } else {
            classCacheDirectory = new File(classCachePath);
        }

        return new ZipClassCache(new File(classCacheDirectory, "cached-robolectric-classes.jar").getAbsolutePath(), AndroidTranslator.CACHE_VERSION);
    }

    public AndroidTranslator createAndroidTranslator(Setup setup, ClassCache classCache) {
        return new AndroidTranslator(classCache, setup);
    }

    protected AndroidManifest createAppManifest() {
        return new AndroidManifest(new File("."));
    }

    public AndroidManifest getAppManifest() {
        return appManifest;
    }

    protected List<AndroidManifest> createLibraryManifests(AndroidManifest appManifest) {
        List<AndroidManifest> manifests = new ArrayList<AndroidManifest>();
        addLibraryManifestsFor(appManifest, manifests);
        return manifests;
    }

    protected void addLibraryManifestsFor(AndroidManifest parentManifest, List<AndroidManifest> manifests) {
        File appBaseDirectory = parentManifest.getResDirectory().getParentFile();

        Properties properties = getProperties(new File(appBaseDirectory, "project.properties"));
        if (properties != null) {
            int libRef = 1;
            String lib;
            while ((lib = properties.getProperty("android.library.reference." + libRef)) != null) {
                File libraryBaseDir = new File(appBaseDirectory, lib);
                AndroidManifest libraryManifest = new AndroidManifest(libraryBaseDir);
                manifests.add(libraryManifest);
                addLibraryManifestsFor(libraryManifest, manifests);
                libRef++;
            }
        }
    }

    public List<AndroidManifest> getLibraryManifests() {
        return libraryManifests;
    }

    private static Properties getProperties(File propertiesFile) {
        if (!propertiesFile.exists()) return null;

        Properties properties = new Properties();
        FileInputStream stream;
        try {
            stream = new FileInputStream(propertiesFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            try {
                properties.load(stream);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    public ClassHandler getClassHandler() {
        return classHandler;
    }

    public List<ResourcePath> getResourcePaths() {
        List<ResourcePath> resourcePaths = new ArrayList<ResourcePath>();
        resourcePaths.add(getAppManifest().getResourcePath());
        for (AndroidManifest libraryManifest : getLibraryManifests()) {
            resourcePaths.add(libraryManifest.getResourcePath());
        }
        resourcePaths.add(ResourceLoader.getSystemResourcePath(getAppManifest().getRealSdkVersion(), resourcePaths));
        return resourcePaths;
    }

    private Class<?> bootstrapTestClass(Class<?> testClass) {
        try {
            return robolectricClassLoader.loadClass(testClass.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public RobolectricTestRunnerInterface getBootstrappedTestRunner(RobolectricTestRunnerInterface originalTestRunner) {
        Class<?> originalTestClass = originalTestRunner.getTestClass().getJavaClass();
        Class<?> bootstrappedTestClass = bootstrapTestClass(originalTestClass);
        Class<?> bootstrappedTestRunnerClass = bootstrapTestClass(originalTestRunner.getClass());

        try {
            Constructor<?> constructorForDelegate = bootstrappedTestRunnerClass.getConstructor(Class.class);
            return (RobolectricTestRunnerInterface) constructorForDelegate.newInstance(bootstrappedTestClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setRobolectricContextField(Class<?> testRunnerClass) {
        Class<?> clazz = testRunnerClass;
        while (!clazz.getName().equals(RobolectricTestRunner.class.getName())) {
            clazz = clazz.getSuperclass();
            if (clazz == null)
                throw new RuntimeException(testRunnerClass + " doesn't extend RobolectricTestRunner");
        }
        try {
            Field field = clazz.getDeclaredField("sharedRobolectricContext");
            field.setAccessible(true);
            field.set(null, this);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected ClassLoader createRobolectricClassLoader(Setup setup, ClassCache classCache, AndroidTranslator androidTranslator) {
//            shadowWrangler.delegateBackToInstrumented = true;
        final ClassLoader parentClassLoader = this.getClass().getClassLoader();
        ClassLoader realAndroidJarsClassLoader = new URLClassLoader(new URL[]{
//                        parseUrl(getAndroidSdkHome() + "/add-ons/addon_google_apis_google_inc_8/libs/maps.jar"),
                getRealAndroidArtifact("android-base"),
                getRealAndroidArtifact("android-kxml2"),
                getRealAndroidArtifact("android-luni")
        }, null) {
            @Override
            public Class<?> loadClass(String s) throws ClassNotFoundException {
                return super.loadClass(s);
            }

            @Override
            protected Class<?> findClass(String s) throws ClassNotFoundException {
                try {
                    return super.findClass(s);
                } catch (ClassNotFoundException e) {
                    return parentClassLoader.loadClass(s);
                }
            }

            @Nullable
            @Override
            public URL getResource(String s) {
                URL resource = super.getResource(s);
                if (resource != null) return resource;
                return parentClassLoader.getResource(s);
            }

            @Override
            public InputStream getResourceAsStream(String s) {
                InputStream resourceAsStream = super.getResourceAsStream(s);
                if (resourceAsStream != null) return resourceAsStream;
                return parentClassLoader.getResourceAsStream(s);
            }

            @Override
            public Enumeration<URL> getResources(String s) throws IOException {
                List<URL> resources = Collections.list(super.getResources(s));
                if (!resources.isEmpty()) return Collections.enumeration(resources);
                return parentClassLoader.getResources(s);
            }
        };
//        InstrumentingClassLoader robolectricClassLoader = new InstrumentingClassLoader(realAndroidJarsClassLoader, classCache, androidTranslator, setup);
        ClassLoader robolectricClassLoader = new AsmInstrumentingClassLoader(setup, realAndroidJarsClassLoader);
        injectClassHandler(robolectricClassLoader);
        return robolectricClassLoader;
    }

    private void injectClassHandler(ClassLoader robolectricClassLoader) {
        try {
            String className = RobolectricInternals.class.getName();
            Class<?> robolectricInternalsClass = robolectricClassLoader.loadClass(className);
            Field field = robolectricInternalsClass.getDeclaredField("classHandler");
            field.setAccessible(true);
            field.set(null, classHandler);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ClassLoader getRobolectricClassLoader() {
        return robolectricClassLoader;
    }

    public Setup createSetup() {
        return new Setup();
    }

    public RepositorySystem createRepositorySystem() {
        try {
            return new DefaultPlexusContainer().lookup(RepositorySystem.class);
        } catch (Exception e) {
            throw new IllegalStateException("dependency injection failed", e);
        }
    }

    public RepositorySystem getRepositorySystem() {
        return repositorySystem == null ? repositorySystem = createRepositorySystem() : repositorySystem;
    }

    private static RepositorySystemSession newSession(RepositorySystem system) {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();
        LocalRepository localRepo = new LocalRepository(new File(System.getProperty("user.home"), ".m2/repository"));
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo));

        return session;
    }

    public RemoteRepository getCentralRepository() {
        return new RemoteRepository("central", "default", "http://repo1.maven.org/maven2/");
    }

    private URL getRealAndroidArtifact(String artifactId) {
        return getArtifact(new DefaultArtifact("com.squareup.robolectric", artifactId, "real", "jar", "4.1.2_r1"));
    }

    private URL getArtifact(String coords) {
        return getArtifact(new DefaultArtifact(coords));
    }

    private URL getArtifact(Artifact artifact) {
        RepositorySystem repositorySystem = getRepositorySystem();
        RepositorySystemSession session = newSession(repositorySystem);
        ArtifactRequest artifactRequest = new ArtifactRequest().setArtifact(artifact);
        artifactRequest.addRepository(getCentralRepository());

        try {
            ArtifactResult artifactResult = repositorySystem.resolveArtifact(session, artifactRequest);
            return parseUrl("file:" + artifactResult.getArtifact().getFile().getAbsolutePath());
        } catch (ArtifactResolutionException e) {
            throw new RuntimeException(e);
        }
    }

    private static URL parseUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /** @deprecated use {@link com.xtremelabs.robolectric.Robolectric.Reflection#setFinalStaticField(Class, String, Object)} */
    public static void setStaticValue(Class<?> clazz, String fieldName, Object value) {
        Robolectric.Reflection.setFinalStaticField(clazz, fieldName, value);
    }
}
