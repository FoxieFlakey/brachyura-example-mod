import java.io.IOException;

import io.github.coolcrabs.brachyura.fabric.FabricMaven;
import io.github.coolcrabs.brachyura.fabric.Intermediary;
import io.github.coolcrabs.brachyura.fabric.Yarn;
import io.github.coolcrabs.brachyura.mappings.MappingHelper;
import io.github.coolcrabs.brachyura.mappings.Namespaces;
import io.github.coolcrabs.brachyura.minecraft.Minecraft;
import io.github.coolcrabs.brachyura.util.Util;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public class Mappings {
  public static MappingTree createIntermediary(String minecraftVersion) {
    return Intermediary.ofMaven(FabricMaven.URL, FabricMaven.intermediary(minecraftVersion)).tree;
  }
  
  public static MappingTree createYarn(String minecraftVersion) {
    return Yarn.ofMaven(FabricMaven.URL, FabricMaven.yarn(minecraftVersion)).tree;
  }
  
  public static MappingTree createMojmap(MappingTree intermediary, VersionMeta mcVersion) {
    try {
      MemoryMappingTree r = new MemoryMappingTree(true);
      intermediary.accept(r);
      Minecraft.getMojmap(mcVersion).accept(r);
      MappingHelper.dropNullInNamespace(r, Namespaces.INTERMEDIARY);
      return r;
    } catch (IOException e) {
      throw Util.sneak(e);
    }
  }
}





