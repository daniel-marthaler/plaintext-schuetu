package ch.plaintext.schuetu.entity;

import ch.plaintext.framework.SuperModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Text Entity (mapped to TEXT2 table)
 */
@Entity
@Table(name = "TEXT2")
@Data
@EqualsAndHashCode(callSuper = false)
public class Text extends SuperModel {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Column(columnDefinition = "text")
    private String value = null;

    @NotNull
    @Column(unique = true)
    private String key = null;

}
