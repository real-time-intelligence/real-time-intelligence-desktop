package ru.rti.desktop.config;

import dagger.Module;
import dagger.Provides;
import javax.inject.Named;
import javax.inject.Singleton;
import ru.rti.desktop.security.EncryptDecrypt;

@Module
public class SecurityConfig {

  @Provides
  @Singleton
  @Named("encryptDecrypt")
  public EncryptDecrypt getEncryptDecrypt() {
    return new EncryptDecrypt();
  }

}
