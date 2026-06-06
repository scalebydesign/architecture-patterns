package com.scalebydesign.cqrs.command;

import java.util.UUID;

public record SubmitOrderCommand(UUID orderId) {}
