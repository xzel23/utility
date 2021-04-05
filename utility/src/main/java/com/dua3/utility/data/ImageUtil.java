package com.dua3.utility.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

public interface ImageUtil<I> {

    String NO_IMPLEMENTATION = "no ImageUtil implementation present";
    
    static ImageUtil getInstance() {
        //noinspection rawtypes
        Iterator<ImageUtil> serviceIterator = ServiceLoader
                .load(ImageUtil.class)
                .iterator();

        ImageUtil<?> iu;
        if (serviceIterator.hasNext()) {
            iu = serviceIterator.next();
        } else {
            iu = new ImageUtil<Void>() {
                @Override
                public Optional<? extends Image> load(InputStream in) {
                    throw new UnsupportedOperationException(NO_IMPLEMENTATION);
                }

                @Override
                public Void convert(Image img) {
                    throw new UnsupportedOperationException(NO_IMPLEMENTATION);
                }

                @Override
                public Image convert(Void img) {
                    throw new UnsupportedOperationException(NO_IMPLEMENTATION);
                }
            };
        }

        return iu;
    }

    Optional<? extends Image> load(InputStream in) throws IOException;

    I convert(Image img);
    
    Image convert(I img);
    
}
