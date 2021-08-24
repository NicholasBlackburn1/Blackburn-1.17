package net.minecraft.world;

import com.google.common.collect.Maps;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.SharedConstants;
import net.minecraft.Util;

public class Snooper {
   private static final String POLL_HOST = "http://snoop.minecraft.net/";
   private static final long DATA_SEND_FREQUENCY = 900000L;
   private static final int SNOOPER_VERSION = 2;
   final Map<String, Object> fixedData = Maps.newHashMap();
   final Map<String, Object> dynamicData = Maps.newHashMap();
   final String token = UUID.randomUUID().toString();
   final URL url;
   final SnooperPopulator populator;
   private final Timer timer = new Timer("Snooper Timer", true);
   final Object lock = new Object();
   private final long startupTime;
   private boolean started;
   int count;

   public Snooper(String p_19219_, SnooperPopulator p_19220_, long p_19221_) {
      try {
         this.url = new URL("http://snoop.minecraft.net/" + p_19219_ + "?version=2");
      } catch (MalformedURLException malformedurlexception) {
         throw new IllegalArgumentException();
      }

      this.populator = p_19220_;
      this.startupTime = p_19221_;
   }

   public void start() {
      if (!this.started) {
      }

   }

   private void populateFixedData() {
      this.setJvmArgs();
      this.setDynamicData("snooper_token", this.token);
      this.setFixedData("snooper_token", this.token);
      this.setFixedData("os_name", System.getProperty("os.name"));
      this.setFixedData("os_version", System.getProperty("os.version"));
      this.setFixedData("os_architecture", System.getProperty("os.arch"));
      this.setFixedData("java_version", System.getProperty("java.version"));
      this.setDynamicData("version", SharedConstants.getCurrentVersion().getId());
      this.populator.populateSnooperInitial(this);
   }

   private void setJvmArgs() {
      int[] aint = new int[]{0};
      Util.getVmArguments().forEach((p_146675_) -> {
         int j = aint[0];
         int i = aint[0];
         aint[0] = j + 1;
         this.setDynamicData("jvm_arg[" + i + "]", p_146675_);
      });
      this.setDynamicData("jvm_args", aint[0]);
   }

   public void prepare() {
      this.setFixedData("memory_total", Runtime.getRuntime().totalMemory());
      this.setFixedData("memory_max", Runtime.getRuntime().maxMemory());
      this.setFixedData("memory_free", Runtime.getRuntime().freeMemory());
      this.setFixedData("cpu_cores", Runtime.getRuntime().availableProcessors());
      this.populator.populateSnooper(this);
   }

   public void setDynamicData(String p_19224_, Object p_19225_) {
      synchronized(this.lock) {
         this.dynamicData.put(p_19224_, p_19225_);
      }
   }

   public void setFixedData(String p_19228_, Object p_19229_) {
      synchronized(this.lock) {
         this.fixedData.put(p_19228_, p_19229_);
      }
   }

   public Map<String, String> getValues() {
      Map<String, String> map = Maps.newLinkedHashMap();
      synchronized(this.lock) {
         this.prepare();

         for(Entry<String, Object> entry : this.fixedData.entrySet()) {
            map.put(entry.getKey(), entry.getValue().toString());
         }

         for(Entry<String, Object> entry1 : this.dynamicData.entrySet()) {
            map.put(entry1.getKey(), entry1.getValue().toString());
         }

         return map;
      }
   }

   public boolean isStarted() {
      return this.started;
   }

   public void interrupt() {
      this.timer.cancel();
   }

   public String getToken() {
      return this.token;
   }

   public long getStartupTime() {
      return this.startupTime;
   }
}