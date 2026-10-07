package com.bookloop.book.dto;
 
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class BookListResponse {

    private List<BookResponse> books;

    private long total;
}