package ch.plaintext.schuetu.service.backupsync;

import lombok.Data;

/**
 * Model for backup sync data
 */
@Data
public class BackupSyncModel {

    private String id;

    private int toreABestaetigt = -1;

    private int toreBBestaetigt = -1;

}
