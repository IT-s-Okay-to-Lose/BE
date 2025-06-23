package com.example.iotl.dto.marketindex;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrentIndexResponseDto {

    @JsonProperty("output")
    private CurrentIndexData output; // 여기에 실제 데이터가 들어옴

    @Getter
    @Setter
    public static class CurrentIndexData {
        @JsonProperty("bstp_nmix_prpr")
        private Double bstp_nmix_prpr;

        @JsonProperty("bstp_nmix_prdy_vrss")
        private Double bstp_nmix_prdy_vrss;

        @JsonProperty("bstp_nmix_prdy_ctrt")
        private Double bstp_nmix_prdy_ctrt;

        @JsonProperty("prdy_vrss_sign")
        private String prdy_vrss_sign;
    }
}