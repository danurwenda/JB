package com.jbatik.imageeditor.filters;

import java.awt.image.BufferedImage;

public interface BufferedImageFilter {

    BufferedImage filter(BufferedImage source, Object...args);
}
