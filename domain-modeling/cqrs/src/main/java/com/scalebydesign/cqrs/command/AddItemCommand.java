package com.scalebydesign.cqrs.command;

import java.math.BigDecimal;
import java.util.UUID;

public record AddItemCommand(UUID orderId, String productId, String name, int quantity, BigDecimal unitPrice) {}
