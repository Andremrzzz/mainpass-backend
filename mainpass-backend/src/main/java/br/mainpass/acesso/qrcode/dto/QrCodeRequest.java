package br.mainpass.acesso.qrcode.dto;

import jakarta.validation.constraints.NotNull;

public record QrCodeRequest(@NotNull Long ambienteId) {
}
