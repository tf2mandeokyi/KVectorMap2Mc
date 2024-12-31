package com.mndk.scjdmc.util.file;

import com.mndk.scjdmc.util.ScjdMapIndexUtils;
import com.mndk.scjdmc.util.ScjdParsedType;
import lombok.Getter;

import java.io.File;

@Getter
public class ScjdFileInformation {
    private final File file;
    private final ScjdParsedType parsedType;

    public ScjdFileInformation(File file, ScjdParsedType parsedType) {
        this.file = file;
        this.parsedType = parsedType;
    }

    public String getNameOrIndex() {
        return switch (parsedType) {
            case INDEX -> ScjdMapIndexUtils.getMapIndexFromFileName(file.getName());
            case TILE -> file.getParentFile().getName() + "," + file.getName();
            default -> file.getName();
        };
    }

    public String getNameForFile() {
        return parsedType + "_" + this.getNameOrIndex();
    }
}
