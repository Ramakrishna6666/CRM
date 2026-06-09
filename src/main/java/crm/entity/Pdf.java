package crm.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity(name = "pdf")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pdf {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    @Size(min = 2)
    private String name;

    @Transient

    // S3 object key for cloud-native storage
    @Column(name = "s3_key")
    private String s3Key;
    private String content;

}
