package app.stolat.collection.internal;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class TagReaderTest {

    @InjectMocks
    private TagReader tagReader;

    @Test
    void shouldReadMetadataFromAudioFile() throws Exception {
        var artistMbid = UUID.randomUUID().toString();
        var albumMbid = UUID.randomUUID().toString();
        var trackMbid = UUID.randomUUID().toString();
        var path = Path.of("/music/Radiohead/OK Computer/02 - Paranoid Android.flac");

        var audioFile = mock(AudioFile.class);
        var tag = mock(Tag.class);
        given(audioFile.getTag()).willReturn(tag);
        given(tag.getFirst(FieldKey.ARTIST)).willReturn("Radiohead");
        given(tag.getFirst(FieldKey.MUSICBRAINZ_ARTISTID)).willReturn(artistMbid);
        given(tag.getFirst(FieldKey.ALBUM)).willReturn("OK Computer");
        given(tag.getFirst(FieldKey.MUSICBRAINZ_RELEASE_GROUP_ID)).willReturn(albumMbid);
        given(tag.getFirst(FieldKey.TITLE)).willReturn("Paranoid Android");
        given(tag.getFirst(FieldKey.TRACK)).willReturn("2");
        given(tag.getFirst(FieldKey.DISC_NO)).willReturn("1");
        given(tag.getFirst(FieldKey.MUSICBRAINZ_TRACK_ID)).willReturn(trackMbid);

        try (MockedStatic<AudioFileIO> audioFileIO = mockStatic(AudioFileIO.class)) {
            audioFileIO.when(() -> AudioFileIO.read(path.toFile())).thenReturn(audioFile);

            var metadata = tagReader.read(path);

            assertThat(metadata).isPresent();
            assertThat(metadata.get().artistName()).isEqualTo("Radiohead");
            assertThat(metadata.get().artistMusicBrainzId()).hasToString(artistMbid);
            assertThat(metadata.get().albumTitle()).isEqualTo("OK Computer");
            assertThat(metadata.get().albumMusicBrainzId()).hasToString(albumMbid);
            assertThat(metadata.get().trackTitle()).isEqualTo("Paranoid Android");
            assertThat(metadata.get().trackNumber()).isEqualTo(2);
            assertThat(metadata.get().discNumber()).isEqualTo(1);
            assertThat(metadata.get().trackMusicBrainzId()).hasToString(trackMbid);
        }
    }

    @Test
    void shouldReturnEmptyWhenTagIsMissing() throws Exception {
        var path = Path.of("/music/unknown.flac");
        var audioFile = mock(AudioFile.class);
        given(audioFile.getTag()).willReturn(null);

        try (MockedStatic<AudioFileIO> audioFileIO = mockStatic(AudioFileIO.class)) {
            audioFileIO.when(() -> AudioFileIO.read(path.toFile())).thenReturn(audioFile);

            var metadata = tagReader.read(path);

            assertThat(metadata).isEmpty();
        }
    }
}
