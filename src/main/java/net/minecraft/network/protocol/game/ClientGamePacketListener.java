package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketListener;

public interface ClientGamePacketListener extends PacketListener
{
    void handleAddEntity(ClientboundAddEntityPacket pPacket);

    void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket pPacket);

    void handleAddVibrationSignal(ClientboundAddVibrationSignalPacket p_178542_);

    void handleAddMob(ClientboundAddMobPacket pPacket);

    void handleAddObjective(ClientboundSetObjectivePacket pPacket);

    void handleAddPainting(ClientboundAddPaintingPacket pPacket);

    void handleAddPlayer(ClientboundAddPlayerPacket pPacket);

    void handleAnimate(ClientboundAnimatePacket pPacket);

    void handleAwardStats(ClientboundAwardStatsPacket pPacket);

    void handleAddOrRemoveRecipes(ClientboundRecipePacket pPacket);

    void handleBlockDestruction(ClientboundBlockDestructionPacket pPacket);

    void handleOpenSignEditor(ClientboundOpenSignEditorPacket pPacket);

    void handleBlockEntityData(ClientboundBlockEntityDataPacket pPacket);

    void handleBlockEvent(ClientboundBlockEventPacket pPacket);

    void handleBlockUpdate(ClientboundBlockUpdatePacket pPacket);

    void handleChat(ClientboundChatPacket pPacket);

    void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket pPacket);

    void handleMapItemData(ClientboundMapItemDataPacket pPacket);

    void handleContainerClose(ClientboundContainerClosePacket pPacket);

    void handleContainerContent(ClientboundContainerSetContentPacket pPacket);

    void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket pPacket);

    void handleContainerSetData(ClientboundContainerSetDataPacket pPacket);

    void handleContainerSetSlot(ClientboundContainerSetSlotPacket pPacket);

    void handleCustomPayload(ClientboundCustomPayloadPacket pPacket);

    void handleDisconnect(ClientboundDisconnectPacket pPacket);

    void handleEntityEvent(ClientboundEntityEventPacket pPacket);

    void handleEntityLinkPacket(ClientboundSetEntityLinkPacket pPacket);

    void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket pPacket);

    void handleExplosion(ClientboundExplodePacket pPacket);

    void handleGameEvent(ClientboundGameEventPacket pPacket);

    void handleKeepAlive(ClientboundKeepAlivePacket pPacket);

    void handleLevelChunk(ClientboundLevelChunkPacket pPacket);

    void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket pPacket);

    void handleLevelEvent(ClientboundLevelEventPacket pPacket);

    void handleLogin(ClientboundLoginPacket pPacket);

    void handleMoveEntity(ClientboundMoveEntityPacket pPacket);

    void handleMovePlayer(ClientboundPlayerPositionPacket pPacket);

    void handleParticleEvent(ClientboundLevelParticlesPacket pPacket);

    void handlePing(ClientboundPingPacket p_178545_);

    void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket pPacket);

    void handlePlayerInfo(ClientboundPlayerInfoPacket pPacket);

    void m_182047_(ClientboundRemoveEntitiesPacket p_182700_);

    void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket pPacket);

    void handleRespawn(ClientboundRespawnPacket pPacket);

    void handleRotateMob(ClientboundRotateHeadPacket pPacket);

    void handleSetCarriedItem(ClientboundSetCarriedItemPacket pPacket);

    void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket pPacket);

    void handleSetEntityData(ClientboundSetEntityDataPacket pPacket);

    void handleSetEntityMotion(ClientboundSetEntityMotionPacket pPacket);

    void handleSetEquipment(ClientboundSetEquipmentPacket pPacket);

    void handleSetExperience(ClientboundSetExperiencePacket pPacket);

    void handleSetHealth(ClientboundSetHealthPacket pPacket);

    void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket pPacket);

    void handleSetScore(ClientboundSetScorePacket pPacket);

    void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket p_131430_);

    void handleSetTime(ClientboundSetTimePacket pPacket);

    void handleSoundEvent(ClientboundSoundPacket pPacket);

    void handleSoundEntityEvent(ClientboundSoundEntityPacket pPacket);

    void handleCustomSoundEvent(ClientboundCustomSoundPacket pPacket);

    void handleTakeItemEntity(ClientboundTakeItemEntityPacket pPacket);

    void handleTeleportEntity(ClientboundTeleportEntityPacket pPacket);

    void handleUpdateAttributes(ClientboundUpdateAttributesPacket pPacket);

    void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket pPacket);

    void handleUpdateTags(ClientboundUpdateTagsPacket pPacket);

    void handlePlayerCombatEnd(ClientboundPlayerCombatEndPacket p_178546_);

    void handlePlayerCombatEnter(ClientboundPlayerCombatEnterPacket p_178547_);

    void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket p_178548_);

    void handleChangeDifficulty(ClientboundChangeDifficultyPacket pPacket);

    void handleSetCamera(ClientboundSetCameraPacket pPacket);

    void handleInitializeBorder(ClientboundInitializeBorderPacket p_178544_);

    void handleSetBorderLerpSize(ClientboundSetBorderLerpSizePacket p_178552_);

    void handleSetBorderSize(ClientboundSetBorderSizePacket p_178553_);

    void handleSetBorderWarningDelay(ClientboundSetBorderWarningDelayPacket p_178554_);

    void handleSetBorderWarningDistance(ClientboundSetBorderWarningDistancePacket p_178555_);

    void handleSetBorderCenter(ClientboundSetBorderCenterPacket p_178551_);

    void handleTabListCustomisation(ClientboundTabListPacket pPacket);

    void handleResourcePack(ClientboundResourcePackPacket pPacket);

    void handleBossUpdate(ClientboundBossEventPacket pPacket);

    void handleItemCooldown(ClientboundCooldownPacket pPacket);

    void handleMoveVehicle(ClientboundMoveVehiclePacket pPacket);

    void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket pPacket);

    void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket pPacket);

    void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket pPacket);

    void handleCommands(ClientboundCommandsPacket pPacket);

    void handleStopSoundEvent(ClientboundStopSoundPacket pPacket);

    void handleCommandSuggestions(ClientboundCommandSuggestionsPacket pPacket);

    void handleUpdateRecipes(ClientboundUpdateRecipesPacket pPacket);

    void handleLookAt(ClientboundPlayerLookAtPacket pPacket);

    void handleTagQueryPacket(ClientboundTagQueryPacket pPacket);

    void handleLightUpdatePacked(ClientboundLightUpdatePacket pPacket);

    void handleOpenBook(ClientboundOpenBookPacket pPacket);

    void handleOpenScreen(ClientboundOpenScreenPacket pPacket);

    void handleMerchantOffers(ClientboundMerchantOffersPacket pPacket);

    void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket pPacket);

    void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket pPacket);

    void handleBlockBreakAck(ClientboundBlockBreakAckPacket pPacket);

    void setActionBarText(ClientboundSetActionBarTextPacket p_178550_);

    void setSubtitleText(ClientboundSetSubtitleTextPacket p_178556_);

    void setTitleText(ClientboundSetTitleTextPacket p_178557_);

    void setTitlesAnimation(ClientboundSetTitlesAnimationPacket p_178558_);

    void handleTitlesClear(ClientboundClearTitlesPacket p_178543_);
}
