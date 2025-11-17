package ru.chsu.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import ru.chsu.exception.*;
import ru.chsu.mapper.LoanMapper;
import ru.chsu.model.dto.LoanDto;
import ru.chsu.model.dto.RequestLoan;
import ru.chsu.model.dto.UpdateLoan;
import ru.chsu.model.entity.Book;
import ru.chsu.model.entity.Loan;
import ru.chsu.model.entity.Reader;
import ru.chsu.repository.BookRepository;
import ru.chsu.repository.LoanRepository;
import ru.chsu.repository.ReaderRepository;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class LoanService {

    private final LoanRepository loanRepository;
    private final ReaderRepository readerRepository;
    private final BookRepository bookRepository;
    private final LoanMapper loanMapper;

    @Inject
    public LoanService(LoanRepository loanRepository, ReaderRepository readerRepository, BookRepository bookRepository, LoanMapper loanMapper) {
        this.loanRepository = loanRepository;
        this.readerRepository = readerRepository;
        this.bookRepository = bookRepository;
        this.loanMapper = loanMapper;
    }

    public List<LoanDto> getAllLoans() {
        return loanRepository.listAll().stream()
                .map(loanMapper::toDto)
                .toList();
    }

    public LoanDto getLoanById(Long loanId) {
        return loanRepository.findByIdOptional(loanId)
                .map(loanMapper::toDto)
                .orElseThrow(() -> new LoanNotFoundException(loanId));
    }

    @Transactional
    public LoanDto createLoan(RequestLoan dto) {
        Loan loan = new Loan();
        loanProcess(loan, dto);
        loan.setLoanDate(LocalDate.now());
        loan.setReturnDate(null);
        loanRepository.persist(loan);
        return loanMapper.toDto(loan);
    }

    @Transactional
    public LoanDto updateLoan(Long loanId, UpdateLoan dto) {
        Loan loan = loanRepository.findByIdOptional(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));
        if (loan.getReturnDate() != null) {
            if (dto.getLoanDate() != null) {
                loan.setLoanDate(dto.getLoanDate());
            }
            if (dto.getReturnDate() != null) {
                loan.setReturnDate(dto.getReturnDate());
            }
            loanRepository.persist(loan);
            return loanMapper.toDto(loan);
        }
        Book oldBook = loan.getBook();
        if (dto.getBookId() != null && !oldBook.getId().equals(dto.getBookId())) {
            oldBook.setAvailable(true);
            bookRepository.persist(oldBook);
        }
        loanProcess(loan, dto);
        if (dto.getLoanDate() != null) {
            loan.setLoanDate(dto.getLoanDate());
        }
        if (dto.getReturnDate() != null) {
            if (dto.getReturnDate().isAfter(loan.getLoanDate())) {
                loan.setReturnDate(dto.getReturnDate());
            } else {
                throw new ReturnDateException("Return Date is null or earlier than loan date");
            }
        }

        loanRepository.persist(loan);
        return loanMapper.toDto(loan);
    }

    private void loanProcess(Loan loan, RequestLoan dto) {
        if (dto.getBookId() != null) {
            if (loan.getBook() != null && loan.getBook().getId().equals(dto.getBookId())) {
                // книга не изменилась
            } else {
                Book book = bookRepository.findByIdOptional(dto.getBookId())
                        .orElseThrow(() -> new BookNotFoundException(dto.getBookId()));
                if (!book.getAvailable()) {
                    throw new BookUnavailableException("Book is already loaned out");
                }
                book.setAvailable(false);
                bookRepository.persist(book);
                loan.setBook(book);
            }
        }
        if (dto.getReaderId() != null) {
            Reader reader = readerRepository.findByIdOptional(dto.getReaderId())
                    .orElseThrow(() -> new ReaderNotFoundException(dto.getReaderId()));
            loan.setReader(reader);
        }
    }

    @Transactional
    public void deleteLoan(Long loanId) {
        Loan loan = loanRepository.findByIdOptional(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));
        Book book = loan.getBook();
        if (book != null) {
            book.setAvailable(true);
            bookRepository.persist(book);
        }
        loanRepository.delete(loan);
    }

    @Transactional
    public LoanDto returnBook(Long loanId) {
        Loan loan = loanRepository.findByIdOptional(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));
        Book book = loan.getBook();
        book.setAvailable(true);
        bookRepository.persist(book);
        loan.setBook(book);
        loan.setReturnDate(LocalDate.now());
        loanRepository.persist(loan);
        return loanMapper.toDto(loan);
    }
}
