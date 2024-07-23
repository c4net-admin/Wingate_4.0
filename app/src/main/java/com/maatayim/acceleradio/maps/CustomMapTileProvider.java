package com.maatayim.acceleradio.maps;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.maatayim.acceleradio.utils.FileUtils;

import android.os.Environment;

public class CustomMapTileProvider implements TileProvider {

	private static final int TILE_WIDTH = 256;
    private static final int TILE_HEIGHT = 256;
    private static final int BUFFER_SIZE = 16 * 1024;
	private String dir = "someDir/";
	private String fileExtension = ".png";


    public CustomMapTileProvider() {
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        //y = fixYCoordinate(y, zoom);
        byte[] image = readTileImage(x, y, zoom);
        return image == null ? null : new Tile(TILE_WIDTH, TILE_HEIGHT, image);
    }

    private synchronized byte[] readTileImage(int x, int y, int zoom) {
        //InputStream in = null;
        FileInputStream in = null;
        ByteArrayOutputStream buffer = null;

        try {

            in = new FileInputStream(getTileFile(x, y, zoom));
            buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[BUFFER_SIZE];

            while ((nRead = in.read(data, 0, BUFFER_SIZE)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();

            return buffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (Exception ignored) {
                }
            if (buffer != null)
                try {
                    buffer.close();
                } catch (Exception ignored) {
                }
        }
    }

   
//    private int fixYCoordinate(int y, int zoom) {
//        int size = 1 << zoom; // size = 2^zoom
//        return size - 1 - y;
//    }

    private File getTileFile(int x, int y, int zoom) {
        File sdcard = FileUtils.getRootDir();
        String tileFile = "/maps/" + dir + zoom + '/' + x + '/' + y + fileExtension;
        File file = new File(sdcard,tileFile);
        return file;
    }
    
    public void setMapFolder(String dir, String extension){
    	this.dir = dir;
    	fileExtension = extension;
    }
}
