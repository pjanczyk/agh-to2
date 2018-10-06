import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import model.Photo;
import model.PhotoSize;
import util.PhotoDownloader;
import util.PhotoProcessor;
import util.PhotoSerializer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PhotoCrawler {

    private static final Logger log = Logger.getLogger(PhotoCrawler.class.getName());

    private final PhotoDownloader photoDownloader;

    private final PhotoSerializer photoSerializer;

    private final PhotoProcessor photoProcessor;

    public PhotoCrawler() throws IOException {
        this.photoDownloader = new PhotoDownloader();
        this.photoSerializer = new PhotoSerializer("./photos");
        this.photoProcessor = new PhotoProcessor();
    }

    public void resetLibrary() throws IOException {
        photoSerializer.deleteLibraryContents();
    }

    public void downloadPhotoExamples() {
        photoDownloader.getPhotoExamples()
                .compose(this::processPhotos)
                .blockingSubscribe(
                        photoSerializer::savePhoto,
                        this::handleError);
    }

    public void downloadPhotosForQuery(String query) {
        photoDownloader.searchForPhotos(query)
                .compose(this::processPhotos)
                .blockingSubscribe(
                        photoSerializer::savePhoto,
                        this::handleError);
    }

    public void downloadPhotosForMultipleQueries(List<String> queries) {
        photoDownloader.searchForPhotos(queries)
                .compose(this::processPhotos)
                .blockingSubscribe(
                        photoSerializer::savePhoto,
                        this::handleError);
    }

    private void handleError(Throwable e) {
        log.log(Level.SEVERE, "Downloading photos error", e);
    }

    private Observable<Photo> processPhotos(Observable<Photo> photos) {
        return photos
                .filter(photoProcessor::isPhotoValid)
                .groupBy(PhotoSize::resolve)
                .flatMap(group -> {
                    switch (group.getKey()) {
                        case MEDIUM:
                            return group
                                    .buffer(5, TimeUnit.SECONDS)
                                    .doOnNext(buffer -> log.info("Medium photo buffer: " + buffer.size() + " photos"))
                                    .flatMapIterable(x -> x);
                        case LARGE:
                            return group
                                    .observeOn(Schedulers.computation())
                                    .doOnNext(p -> log.info("Converting to miniature on thread: " +
                                            Thread.currentThread().getName()))
                                    .map(photoProcessor::convertToMiniature);
                        default:
                            throw new AssertionError();
                    }
                });
    }

}
