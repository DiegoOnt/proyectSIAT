package com.siat;

public interface AssetUpdateListener {
    void onAssetSaved();
    void onAssetDeleted(int assetId);
}
