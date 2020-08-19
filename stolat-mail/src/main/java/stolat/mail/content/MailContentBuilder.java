package stolat.mail.content;

import stolat.model.BirthdayAlbums;

public interface MailContentBuilder {

    String build(BirthdayAlbums birthdayAlbums);
}
