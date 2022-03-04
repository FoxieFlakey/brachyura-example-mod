import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.fabric.FabricProject;
import io.github.coolcrabs.brachyura.ide.IdeModule;
import io.github.coolcrabs.brachyura.util.Lazy;
import io.github.coolcrabs.brachyura.util.OsUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;

public abstract class SingleSrcDirFabricProject extends FabricProject {
  public abstract List<String> getClientVMArgs();
  public abstract List<String> getServerVMArgs();
  
  @Override
  public IdeModule[] getIdeModules() {
    Path cwd = PathUtil.resolveAndCreateDir(this.getProjectDir(), "run");
    Lazy<List<Path>> classpath = new Lazy<>(() -> {
      Path mappingsClasspath = this.writeMappings4FabricStuff().getParent().getParent();
      ArrayList<Path> r = new ArrayList<>(this.runtimeDependencies.get().size() + 1);
      
      for (JavaJarDependency dependency : this.runtimeDependencies.get()) {
        r.add(dependency.jar);
      }
      
      r.add(mappingsClasspath);
      return r;
    });
    
    Lazy<Path> launchConfig = new Lazy<>(this::writeLaunchCfg);
    
    return new IdeModule[] {
      new IdeModule.IdeModuleBuilder()
        .name(this.getModId())
        .root(this.getProjectDir())
        .javaVersion(this.getJavaVersion())
        .dependencies(this.ideDependencies)
        .sourcePaths(this.getSrcDir())
        .resourcePaths(this.getResourcesDir())
        .runConfigs(
          // Client config
          new IdeModule.RunConfigBuilder()
            .name("Minecraft Client")
            .cwd(cwd)
            .mainClass("net.fabricmc.devlaunchinjector.Main")
            .classpath(classpath)
            .resourcePaths(this.getResourcesDir())
            .vmArgs(
              () -> {
                ArrayList<String> clientArgs = new ArrayList<>(Arrays.asList(
                  "-Dfabric.dli.config=" + launchConfig.get().toString(),
                  "-Dfabric.dli.env=client",
                  "-Dfabric.dli.main=net.fabricmc.loader.launch.knot.KnotClient"
                ));
                clientArgs.addAll(this.getClientVMArgs());
                
                if (OsUtil.OS == OsUtil.Os.OSX) {
                  clientArgs.add("-XstartOnFirstThread");
                }
                return clientArgs;
              }
            ),
          // Server config
          new IdeModule.RunConfigBuilder()
            .name("Minecraft Server")
            .cwd(cwd)
            .mainClass("net.fabricmc.devlaunchinjector.Main")
            .classpath(classpath)
            .resourcePaths(this.getResourcesDir())
            .vmArgs(
              () -> {
                ArrayList<String> serverArgs = new ArrayList<>(Arrays.asList(
                  "-Dfabric.dli.config=" + launchConfig.get().toString(),
                  "-Dfabric.dli.env=client",
                  "-Dfabric.dli.main=net.fabricmc.loader.launch.knot.KnotClient"
                ));
                serverArgs.addAll(this.getServerVMArgs());
                
                return serverArgs;
              }
            )
        )
        .build() 
    };
  }
}
