package com.vitorxp.WorthClient.interfaces;

import java.io.File;

public interface IResourcePackRepository {
    void navigateTo(File newDir);
    File getCurrentDirectory();
}