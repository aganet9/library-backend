package ru.chsu.controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import ru.chsu.model.dto.BookDto;
import ru.chsu.model.dto.RequestBook;
import ru.chsu.service.BookService;

import java.util.List;

@Path("/api/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookController {

    private final BookService bookService;

    @Inject
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GET
    public List<BookDto> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GET
    @Path("/{id}")
    public BookDto getBookById(@PathParam("id") Long id) {
        return bookService.getBookById(id);
    }

    @POST
    public BookDto createBook(@Valid RequestBook dto) {
        return bookService.createBook(dto);
    }

    @PUT
    @Path("/{id}")
    public BookDto updateBook(@PathParam("id") Long id, @Valid RequestBook dto) {
        return bookService.updateBook(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public void deleteBook(@PathParam("id") Long id) {
        bookService.deleteBook(id);
    }

    @POST
    @Path("/{bookId}/genres/{genreName}")
    public BookDto addGenreToBook(@PathParam("bookId") Long bookId,
                                  @PathParam("genreName") String genreName) {
        return bookService.addGenre(bookId, genreName);
    }

    @DELETE
    @Path("/{bookId}/genres/{genreName}")
    public void removeGenreFromBook(@PathParam("bookId") Long bookId,
                                    @PathParam("genreName") String genreName) {
        bookService.removeGenre(bookId, genreName);
    }

    @PATCH
    @Path("/{id}/title/{titleName}")
    public BookDto changeTitle(@PathParam("id") Long id, @PathParam("titleName") String title) {
        return bookService.changeTitle(id, title);
    }
}
