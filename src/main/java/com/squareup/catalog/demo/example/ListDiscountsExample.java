/*
 * Copyright 2017 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.catalog.demo.example;

import java.util.List;

import com.squareup.catalog.demo.Logger;
import com.squareup.catalog.demo.util.CatalogObjectTypes;
import com.squareup.catalog.demo.util.DiscountTypes;
import com.squareup.square.exceptions.ApiException;
import com.squareup.square.api.CatalogApi;
import com.squareup.square.api.LocationsApi;
import com.squareup.square.models.CatalogDiscount;
import com.squareup.square.models.CatalogObject;
import com.squareup.catalog.demo.util.Moneys;


/**
 * This example lists all discounts in the catalog.
 */
public class ListDiscountsExample extends Example {

  private String cursor = null;

  public ListDiscountsExample(Logger logger) {
    super("list_discounts", "List all discounts.", logger);
  }

  @Override
  public void execute(CatalogApi catalogApi, LocationsApi locationsApi) throws ApiException {

    // Optional parameters can be set to null.
    Long catalogVersion = null;

    do {
        // Retrieve a page of discounts.
        catalogApi.listCatalogAsync(cursor, CatalogObjectTypes.DISCOUNT.toString(), catalogVersion).thenAccept(result -> {
            if (checkAndLogErrors(result.getErrors())) {
                return;
            }

            List<CatalogObject> discounts = result.getObjects();
            if (discounts == null || discounts.size() == 0) {
                if (cursor == null) {
                    logger.info("No discounts found.");
                    return;
                }
            } else {
                for (CatalogObject discountObject : discounts) {
                    CatalogDiscount discount = discountObject.getDiscountData();
                    String amount = null;

                     // Determine which type of discount this is.
                    switch (DiscountTypes.valueOf(discount.getDiscountType())) {
                        case FIXED_AMOUNT:
                            amount = Moneys.format(discount.getAmountMoney());
                            break;
                        case FIXED_PERCENTAGE:
                            amount = discount.getPercentage() + "%";
                            break;
                        case VARIABLE_AMOUNT:
                            amount = "variable $";
                            break;
                        case VARIABLE_PERCENTAGE:
                            amount = "variable %";
                            break;
                    }

                    // Log the name and amount of the discount.
                    String logMessage = discount.getName();
                    if (amount != null) {
                        logMessage += " [" + amount + "]";
                    }
                    logMessage += " (" + discountObject.getId() + ")";
                    logger.info(logMessage);
                }
            }
            cursor = result.getCursor();
        }).exceptionally(exception -> {
            // Log exception, return null.
            logger.error(exception.getMessage());
            return null;
        }).join();
    } while (cursor != null);
  }
}
