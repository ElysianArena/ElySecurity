package cn.ElysianArena.ElySecurity.security;

public interface SecurityModule {
    void onEnable();
    void onDisable();
    void registerEvents();
    String getModuleName();
}