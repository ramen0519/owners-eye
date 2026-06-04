package com.ownerseye.ownerseye.domain.menu_sale.exception;

import com.ownerseye.ownerseye.domain.menu_sale.exception.code.MenuSaleErrorCode;
import com.ownerseye.ownerseye.global.exception.AppException;

public class MenuSaleException extends AppException {

    public MenuSaleException(MenuSaleErrorCode errorCode) {
        super(errorCode);
    }
}
