package io.github.colochampre.riskofrain_mobs.fabric.client;

import io.github.colochampre.riskofrain_mobs.client.RoRModClient;
import net.fabricmc.api.ClientModInitializer;

public final class RoRFabricClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    RoRModClient.init();
  }
}
