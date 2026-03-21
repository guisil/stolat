package app.stolat.collection;

import java.util.List;

import app.stolat.collection.internal.AlbumRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CollectionService {

    private final AlbumRepository albumRepository;

    public CollectionService(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    public List<Album> findAllAlbums() {
        return albumRepository.findAll();
    }
}
