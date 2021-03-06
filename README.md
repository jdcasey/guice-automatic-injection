#guice-automatic-injection

Google Guice-Extension for automatic Modules and Beans Binding.

##Blog-Entries
[Part 1](http://devsurf.wordpress.com/2010/09/06/google-guice-classpath-scanning-and-automatic-beans-binding-and-module-installation/)  
[Part 2](http://devsurf.wordpress.com/2010/09/07/guice-automatic-injection-binding-listeners-parallel-scanning/)  
[Part 3](http://devsurf.wordpress.com/2010/09/09/guice-automatic-injectionbinding-jsr330-fighting-with-maven-github-and-windows/)  
[Part 4](http://devsurf.wordpress.com/2010/09/15/guice-automatic-injectionbinding-guicyfruit-integration-postconstructpredestroy-guicejndi-and-the-childinjector/)  
[Part 5](http://devsurf.wordpress.com/2010/09/27/automatic-binding-for-guice-release-v0-7-aop-configuration-rocoto/)  


[Ohloh.net](https://www.ohloh.net/p/guice-auto-injection)  
[DZone](http://www.dzone.com/links/quick_tip_automatic_injectionbinding_for_google_g.html)  
[TheServerSide](http://www.theserverside.com/news/thread.tss?thread_id=60851)  
[Github](git://github.com/manzke/guice-automatic-injection.git)  
 
 

##Automatic-Injection

This is the Core module which defines the Interfaces used to create Classpath Scanner implementations and dynamic Binders.
Existing implementations are Reflections/Javassit, a Sonatype-Extension and my own implementation based 
on ASM.

##Advantages
- No manual Binding of Beans, Modules, Configurations, ... just annotate it
- Classpath Scanning with your Scanner choice: ASM-based, Sonatype, Reflections, ...
- Reuse Method-Interceptors of AOP-Alliance (just add Annotations)
- Use JNDI-Context to have no Guice-Dependencies in your Code (new InitialContext().lookup(..))
- Use common Guice-Extensions (GuicyFruit, rocoto, Apache Commons Configuration)

###Example
Base for our Examples is the Example interface...

	public interface Example {
		String sayHello();
	}

...and our Example-Application...

	public class ExampleApp {
		public static void main( String[] args ) throws IOException {
			Injector injector = Guice.createInjector(StartupModule.create(VirtualClasspathReader.class, PackageFilter.create("de.devsurf")));
			System.out.println(injector.getInstance(Example.class).sayHello());
		}
	}

...which shows, how to use the automatic Injection.

First of all you have to create a StartupModule and pass the Class of the ClasspathScanner you want to use. As 
a second Parameter you can specify which Packages should be scanned. Not all Scanner will support this feature,
so it can be, that the Packages get ignored. 

####Automatic Binding-Example
To use our AutoBind-Annotation you just have to annotate our Implementation...

	@Bind
	public class ExampleImpl implements Example {
		@Override
		public String sayHello() {
			return "yeahhh!!!";
		}
	}

...so this Class will be registered by our Startup/Scanner-Module and will be bound to all inherited interfaces. If you want that your Class should also be named, 
you have to set the name-Attribute...

	@Named("Example") or @Bind(name=@Named("Example"))

...this will create a Key for the Binding. You can also overwrite the interfaces it should be bound to...

	@Bind(to=@To(customs={Example.class}, value=CUSTOM)

...by passing the Interfaces to the bind()-Attribute.  


####GuiceModule-Example
If you have enough to register every Guice-Module by your own, just annotate it with the @GuiceModule and the Startup/Scanner-Module will install it.

	@GuiceModule
	public class ExampleModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(Example.class).to(ExampleImpl.class);
		}
	}  
	
	
####Overwrite Features-Example
If you want to overwrite, which Features should be activated or if you want to add your own, you have to overwrite the abstract StartupModule.

	public class ExampleStartupModule extends StartupModule {
		public DefaultStartupModule(Class<? extends ClasspathScanner> scanner, String... packages) {
			super(scanner, packages);
		}

		@Override
		protected void bindAnnotationListeners() {
			Multibinder<AnnotationListener> listeners = Multibinder.newSetBinder(binder(), AnnotationListener.class);
			listeners.addBinding().to(AutoBindingFeature.class); //Automatic Beans Binding
			listeners.addBinding().to(ImplementationBindingFeature.class); //Implementation only Binding
			listeners.addBinding().to(MultiBindingFeature.class); //Multiple Binding
			listeners.addBinding().to(ModuleBindingFeature.class); //Automatic Module Installation
		}
	}  


####Use Multibinding-Example
If you want to use Multibind, just annotate your class with @AutoBind and @MultipleBinding.

	@Bind(multiple=true)
	public class ExampleOneImpl implements Example {  
		@Override
		public String sayHello() {
			return "one - yeahhh!!!";  
		}
	}
	
	public class ExampleContainer {
		private List<Example> _examples;
    
		@Inject
		public ExampleContainer(Set<Example> example) {
			_examples = new ArrayList<Example>(example);
		}
    
		public void sayHello(){
			for(Example example : _examples){
				System.out.println(example.sayHello());
			}
		}
	}
	
####Use JSR250-Annotations with GuicyFruit  
GuicyFruit gives you the possibilities to use the Annotations declared in the JSR250. You can annotate your Implementations with @Resource, @PostConstruct and @PreDestroy.

	public interface Example {
		String sayHello(); //will be called in our Application
		void inform(); //will be called through @PostConstruct
	}

	public class ExampleImpl implements Example {
		@PostConstruct
		public void inform(){
			System.out.println("inform about post construction!");
		}  

		@Override
		public String sayHello() {
			return "yeahhh!!!";
		}
	}
	
Inform will be called after the Instance was create by the Injector.

	@GuiceModule
	public class ExampleModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(Example.class).to(ExampleImpl.class);
		}
	}

The ExampleModule will be automatically be bound, by our ClasspathScanner.
	
	public class ExampleApplication{
		public static void main(String[] args) throws IOException {
			StartupModule startupModule = StartupModule.create(VirtualClasspathReader.class, PackageFilter.create(ExampleApp.class), PackageFilter.create(JSR250Module.class));
			Injector injector = Guice.createInjector(startupModule);  
			System.out.println(injector.getInstance(Example.class).sayHello());
		}
	}

And last but not least, our ExampleApplication which creates a new StartupModule, which will bind our ClasspathScanner and the Packages to the Injector. With the help of this Injector we create a new DynamicModule, which is bound to ScannerModule.

####Guicefy JNDI with GuicyFruit
For using JNDI+Guice you have to create a "jndi.properties" and put it in your Classpath. Specify which ContextFactory and ClasspathScanner should be used and where they should scan for Modules and Implementations, which should be installed/bound.

	java.naming.factory.initial = ...integrations.guicyfruit.GuicyInitialContextFactory
	guice.classpath.scanner = ...scanner.asm.VirtualClasspathReader
	guice.classpath.packages = ...

After that you can create a new InitialContext and with some Magic everything can be retrieved by the Context like using an Injector.

	public static void main(String[] args) throws Exception {
		InitialContext context = new InitialContext();
		Example example = (Example) context.lookup(Example.class.getName());  

		System.out.println(example.sayHello());
	}


##TODOs:
- Test Automatic Binding under Linux (v0.8/0.9)
	- in Java Application
	- in Web Application
- Automatic Binding of Configuration
	- add reloading feature
- Stabilize APIs and Code Quality (v0.8/0.9)
- Add parallel binding for Sonatype and pure Implementation (release 1.x)
- Add a Clojure Classpath Scanner (release 1.x)
- Implement Spring-Annotation Binder (automatic Binding of Beans to Guice annotated with Spring-Annotations) (release 1.x)
- use Java EE 6 Annotations for Automatic Binding (Interceptor, AroundInvoke, ...) (release 1.x)