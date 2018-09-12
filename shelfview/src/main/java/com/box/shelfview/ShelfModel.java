package com.box.shelfview;

class ShelfModel {
    private String bookCoverSource;
    private String bookId;
    private String bookTitle;
    private Boolean show;
    private ShelfType type;
    private BookSource bookSource;
    private int badgeCount = 0;

    ShelfModel(String bookCoverSource, String bookId, String bookTitle, Boolean show, ShelfType type, BookSource bookSource) {
        this.bookCoverSource = bookCoverSource;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.show = show;
        this.type = type;
        this.bookSource = bookSource;
    }

    ShelfModel(String bookCoverSource, String bookId, String bookTitle, Boolean show, ShelfType type, BookSource bookSource, int count) {
        this.bookCoverSource = bookCoverSource;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.show = show;
        this.type = type;
        this.bookSource = bookSource;
        this.badgeCount = count;
    }

    public String getBookCoverSource() {
        return bookCoverSource;
    }

    public void setBookCoverSource(String bookCoverSource) {
        this.bookCoverSource = bookCoverSource;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
    }

    public ShelfType getType() {
        return type;
    }

    public void setType(ShelfType type) {
        this.type = type;
    }

    public BookSource getBookSource() {
        return bookSource;
    }

    public void setBookSource(BookSource bookSource) {
        this.bookSource = bookSource;
    }

    public int getBadgeCount() {
        return badgeCount;
    }
}

