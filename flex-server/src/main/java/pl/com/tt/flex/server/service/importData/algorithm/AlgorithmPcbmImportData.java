package pl.com.tt.flex.server.service.importData.algorithm;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@Builder
public class AlgorithmPcbmImportData implements Serializable {
    private String derName;
    private String type;
    private String productType;
    private String power;
    private String price;
}
