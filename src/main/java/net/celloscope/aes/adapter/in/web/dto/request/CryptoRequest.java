package net.celloscope.aes.adapter.in.web.dto.request;

public interface CryptoRequest {

    String data();

    String secretKey();

    String iv();
}
