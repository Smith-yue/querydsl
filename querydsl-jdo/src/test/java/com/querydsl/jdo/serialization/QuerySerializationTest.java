/*
 * Copyright 2011, Mysema Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.querydsl.jdo.serialization;

import static com.querydsl.jdo.JDOExpressions.select;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.querydsl.jdo.test.domain.Book;
import com.querydsl.jdo.test.domain.QProduct;

public class QuerySerializationTest extends AbstractTest{

    private QProduct product = QProduct.product;

    private QProduct other = new QProduct("other");

    @Test
    public void SelectFromWhereOrder() {
        assertEquals(
            "SELECT this.name " +
            "FROM com.querydsl.jdo.test.domain.Product " +
            "WHERE this.name == a1 " +
            "PARAMETERS java.lang.String a1 " +
            "ORDER BY this.name ASC",

            serialize(select(product.name).from(product)
              .where(product.name.eq("Test"))
              .orderBy(product.name.asc())));
    }

    @Test
    public void SelectFromWhereGroupBy() {
        assertEquals(
            "SELECT this.name " +
            "FROM com.querydsl.jdo.test.domain.Product " +
            "WHERE this.name.startsWith(a1) || this.name.endsWith(a2) " +
            "PARAMETERS java.lang.String a1, java.lang.String a2 " +
            "GROUP BY this.price",

            serialize(select(product.name).from(product)
              .where(product.name.startsWith("A").or(product.name.endsWith("B")))
              .groupBy(product.price)));
    }

    @Test
    public void SelectFrom2Sources() {
        assertEquals(
            "SELECT this.name " +
            "FROM com.querydsl.jdo.test.domain.Product " +
            "WHERE this.name == other.name " +
            "VARIABLES com.querydsl.jdo.test.domain.Product other",

            serialize(select(product.name).from(product, other)
              .where(product.name.eq(other.name))));
    }

    @Test
    public void WithSubQuery() {
        assertEquals(
            "SELECT this.price " +
            "FROM com.querydsl.jdo.test.domain.Product " +
            "WHERE this.price < " +
            "(SELECT avg(other.price) FROM com.querydsl.jdo.test.domain.Product other)",

            serialize(select(product.price).from(product)
              .where(product.price.lt(select(other.price.avg()).from(other)))));
    }

    @Test
    public void WithSubQuery2() {
        // FIXME : how to model this ?!?
        assertEquals(
            "SELECT this.name " +
            "FROM com.querydsl.jdo.test.domain.Product " +
            "WHERE (SELECT other.price FROM com.querydsl.jdo.test.domain.Product other " +
                "WHERE other.name == a1 " +
                "PARAMETERS java.lang.String a1).contains(this.price)",

            serialize(select(product.name).from(product)
              .where(product.price.in(select(other.price).from(other).where(other.name.eq("Some name"))))));
    }

    @Test
    public void InstanceofQuery() {
        assertEquals(
            "SELECT " +
            "FROM com.querydsl.jdo.test.domain.Product " +
            "WHERE this instanceof com.querydsl.jdo.test.domain.Book",

            serialize(select(product).from(product)
              .where(product.instanceOf(Book.class))));
    }

}
