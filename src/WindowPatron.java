import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import oracle.jdbc.OracleTypes;
import oracle.jdbc.OracleCallableStatement;

public class WindowPatron {
    JFrame mainWindowJFrame;
    private JTextField bookTitleJTxt, authorNameJTxt, pubYearJTxt, bookISBMJTxt, bookStatusJTxt;
    JLabel bookTitleJLbl, authorNameJLbl, pubYearJLbl, bookISBNJLbl, bookStatusJLbl;
    static private JTable jTable;
    JPanel leftBookFormPanel;
    JButton btnBorrowBook, btnSearchBookMain, btnMyBooks, btnReturnBook, btnRefresh, btnSettings, btnCancel;

    final static int J_TABLE_WIDTH = 900;

    private static JButton btnSearch;
    Connection conn = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    static DefaultTableModel model = new DefaultTableModel();
    private final static String[] TABLE_COLUMNS = {
            "BookTitle", "BookAuthorName", "BookPublicationYear", "BookISBN", "BookStatus", "BookID"
    };

    public WindowPatron() {
        run();
        initModel();
        sort();
        refreshTable();
    }

    private void initModel() {
        Object[] col = TABLE_COLUMNS;
        model.setColumnIdentifiers(col);
        jTable.setModel(model);
    }

    private void sort() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<DefaultTableModel>(model);
        jTable.setRowSorter(sorter);
    }

    private void run() {
        mainWindowJFrame = createMainJFrame("Library Management System");

        leftBookFormPanel = buildBookFormJPanel();
//        JPanel rightBookTablePanel = new JPanel();
        JTextArea infoTxt = new JTextArea();

        /*rightBookTablePanel.setBounds(10, 170, 244, 143);
        rightBookTablePanel.setLayout(null);
        rightBookTablePanel.add(infoTxt);*/

        infoTxt.setEditable(false);
        infoTxt.setBounds(0, 0, 244, 143);

        mainWindowJFrame.add(leftBookFormPanel);
//        mainWindowJFrame.add(rightBookTablePanel);

        initializeButtons();

        addButtonsToBookFrame();

        btnSearchBookMain.addActionListener(e -> onBtnSearchLibraryClick());

        btnMyBooks.addActionListener(e -> {
            conn = DBConnection.getConnection();

            if(conn != null) {

                /** @DOGGO */
                // Call procedure in Oracle
                int uID = CredentialData.getUserLoginID();
                // Declare var 1 with colon
                String sql = "BEGIN :1 := GetBorrowedBooks(" + uID + "); END;";

                try {
                    OracleCallableStatement st = (OracleCallableStatement) conn.prepareCall(sql); // Initialize Statement
                    st.registerOutParameter(1, OracleTypes.CURSOR); // Configure our var 1
                    System.out.println("EXECUTING Function: " + sql + " " + st.execute()); // execute and print to console
                    ResultSet rs = st.getCursor(1); // get result set
                    Object[] columnData = new Object[7];
                    model.setRowCount(0);

                    System.out.println("ROWWWWWWWWWWWWWWWWWWWW" + rs.getRow());

                    // get data

                    if (rs.next())
                        do {
                            columnData[0] = rs.getString("BookTitle");
                            columnData[1] = rs.getString("BookAuthorName");
                            columnData[2] = rs.getString("BookPublicationYear");
                            columnData[3] = rs.getString("BookISBN");
                            columnData[4] = rs.getString("BookStatus");
                            model.addRow(columnData);
                        } while(rs.next());
                    else
                        NotificationManager.Message("Message", "You do not have any borrowed books.");

                } catch (Exception e1) {
                    NotificationManager.Warning("[refreshTable] " + e1.getMessage());
                }
            }
        });

        btnBorrowBook.addActionListener(e -> onBtnBorrowBookClick());

        btnSettings.addActionListener(e -> {
            mainWindowJFrame.dispose();
            new WindowSettings();
        });

        btnReturnBook.addActionListener(e -> onBtnReturnBookClick());

        btnRefresh.addActionListener(e -> refreshTable());

        jTable = new JTable();
        jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jTable.setModel(new DefaultTableModel(
                new Object[][] {},
                TABLE_COLUMNS
        ));

        JScrollPane scrollPane = new JScrollPane(jTable);
        scrollPane.setBounds(264, 11, J_TABLE_WIDTH, 489);
        mainWindowJFrame.add(scrollPane);

    }

    private JFrame createMainJFrame(String title) {
        JFrame mJFrame = new JFrame(title);
        mJFrame.setBounds(100, 100, J_TABLE_WIDTH + 280, 550);
        mJFrame.setLayout(null);
        mJFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // automatic close when different frame
        mJFrame.setLocationRelativeTo(null);
        mJFrame.setResizable(false);
        mJFrame.setVisible(true);
        return mJFrame;
    }

    private void addButtonsToBookFrame() {
        mainWindowJFrame.add(btnSearchBookMain);
        mainWindowJFrame.add(btnMyBooks);
        mainWindowJFrame.add(btnBorrowBook);
        mainWindowJFrame.add(btnReturnBook);
        mainWindowJFrame.add(btnRefresh);
        mainWindowJFrame.add(btnSettings);
    }

    private void initializeButtons() {
        btnSearchBookMain = new JButton("Search Library");
        btnMyBooks = new JButton("My Books");
        btnBorrowBook = new JButton("Borrow Book");
        btnReturnBook = new JButton("Return Book");
        btnRefresh = new JButton("Refresh Data");
        btnSettings = new JButton("Settings");

        btnSearchBookMain.setBounds(10, 11, 244, 23);
        btnMyBooks.setBounds(10, 36, 244, 23);
        btnBorrowBook.setBounds(10, 61, 244, 23);
        btnReturnBook.setBounds(10, 86, 244, 23);
        btnRefresh.setBounds(10, 111, 244, 23);
        btnSettings.setBounds(10, 136, 244, 23);
    }

    private void initializeLabels() {
        bookTitleJLbl = new JLabel("Title");
        authorNameJLbl = new JLabel("Author");
        pubYearJLbl = new JLabel("Pub. Year");
        bookISBNJLbl = new JLabel("ISBN");
        bookStatusJLbl = new JLabel("Status");

        bookTitleJLbl.setBounds(0, 25, 46, 14);
        authorNameJLbl.setBounds(0, 50, 70, 14);
        pubYearJLbl.setBounds(0, 75, 89, 14);
        bookISBNJLbl.setBounds(0, 100, 46, 14);
        bookStatusJLbl.setBounds(0, 125, 46, 14);
    }

    private void initializeTextFields() {
        bookTitleJTxt = new JTextField();
        authorNameJTxt = new JTextField();
        pubYearJTxt = new JTextField();
        bookISBMJTxt = new JTextField();
        bookStatusJTxt = new JTextField();

        bookTitleJTxt.setBounds(119, 25, 125, 20);
        authorNameJTxt.setBounds(119, 50, 125, 20);
        pubYearJTxt.setBounds(119, 75, 125, 20);
        bookISBMJTxt.setBounds(119, 100, 125, 20);
        bookStatusJTxt.setBounds(119, 125, 125, 20);
    }

    private JPanel buildBookFormJPanel() {
        JPanel ms = new JPanel();

        initializeLabels();
        initializeTextFields();

        ms.setBounds(10, 324, 244, 176);
        ms.setLayout(null);

        // all buttons within the book Form
        ms.add(bookTitleJLbl);
        ms.add(authorNameJLbl);
        ms.add(pubYearJLbl);
        ms.add(bookISBNJLbl);
        ms.add(bookStatusJLbl);

        ms.add(pubYearJTxt);
        ms.add(bookTitleJTxt);
        ms.add(authorNameJTxt);
        ms.add(bookISBMJTxt);
        ms.add(bookStatusJTxt);
        ms.setVisible(false);

        return ms;
    }

    // update the contents of the table
    public void refreshTable() {
        conn = DBConnection.getConnection();

        if(conn != null) {

            String sql = "SELECT * FROM Book";
            System.out.println("refreshTable- SQL : " + sql);

            try {
                pst = conn.prepareStatement(sql);
                rs = pst.executeQuery();
                Object [] columnData = new Object[7];
                model.setRowCount(0);

                while (rs.next()) {
                    columnData[0] = rs.getString("BookTitle");
                    columnData[1] = rs.getString("BookAuthorName");
                    columnData[2] = rs.getString("BookPublicationYear");
                    columnData[3] = rs.getString("BookISBN");
                    columnData[4] = rs.getString("BookStatus");
                    columnData[5] = rs.getString("BookID");
                    model.addRow(columnData);
                }
            } catch (Exception e) {
                NotificationManager.Warning("[refreshTable] " + e.getMessage());
            }
        }

    }

    public void searchLibrary(BookObject bo) {
        conn = DBConnection.getConnection();

        if(conn != null) {

            String sql = "SELECT * FROM Book WHERE ";
            sql += "BookTitle LIKE " + "'" + bo.getForSearchQuery(bo.getTitle()) + "' OR ";
            sql += "BookAuthorName LIKE " + "'" + bo.getForSearchQuery(bo.getAuthor()) + "' OR ";
            sql += "BookPublicationYear LIKE " + "'" + bo.getForSearchQuery(bo.getPubYear()) + "' OR ";
            sql += "BookISBN LIKE " + "'" + bo.getForSearchQuery(bo.getIsbn()) + "' OR ";
            sql += "BookStatus LIKE " + "'" + bo.getForSearchQuery(bo.getStatus()) + "'";
            System.out.println("refreshTable- SQL : " + sql);

            try {
                pst = conn.prepareStatement(sql);
                rs = pst.executeQuery();
                Object [] columnData = new Object[7];
                model.setRowCount(0);

                while (rs.next()) {
                    columnData[0] = rs.getString("BookTitle");
                    columnData[1] = rs.getString("BookAuthorName");
                    columnData[2] = rs.getString("BookPublicationYear");
                    columnData[3] = rs.getString("BookISBN");
                    columnData[4] = rs.getString("BookStatus");
                    model.addRow(columnData);
                }
            } catch (Exception e) {
                NotificationManager.Warning("[refreshTable] " + e.getMessage());
            }
        }

    }

    private void onBtnSearchLibraryClick() {
        leftBookFormPanel.setVisible(true);

        btnSearch = new JButton("Search"); // make this single instance
        btnSearch.setEnabled(true);
        btnSearch.addActionListener(e1 -> {
            searchLibrary(createBookObject());
            leftBookFormPanel.setVisible(false);
        });

        btnSearch.setBounds(0, 153, 116, 23);

        btnCancel = new JButton("Cancel");
        btnCancel.setBounds(128, 153, 116, 23);
        btnCancel.addActionListener(e -> {
            pubYearJTxt.setText("");
            authorNameJTxt.setText("");
            bookTitleJTxt.setText("");
            bookISBMJTxt.setText("");
            bookStatusJTxt.setText("");

            leftBookFormPanel.setVisible(false);
        });

        leftBookFormPanel.add(btnSearch);
        leftBookFormPanel.add(btnCancel);

        leftBookFormPanel.revalidate(); // update changes
        leftBookFormPanel.repaint(); // update changes
    }

    private void onBtnBorrowBookClick() {
        DefaultTableModel model = (DefaultTableModel) jTable.getModel();

        if (jTable.getSelectedRow() < 0) {
            NotificationManager.Error("Select a book first");
        } else {
            System.out.println("Borrow Book Button Pressed");
            int i = jTable.getSelectedRow();
            /*System.out.println("Book Information: \n"
                    + "\nTitle:\t" + model.getValueAt(i, 0).toString()
                    + "\nAuthor Name:\t" + model.getValueAt(i, 1).toString()
                    + "\nPublication Year:\t" + model.getValueAt(i, 2).toString()
                    + "\nISBN:\t" + model.getValueAt(i, 3).toString()
                    + "\nStatus:\t" + model.getValueAt(i, 4).toString());*/

                try {
                    String dateToday = new SimpleDateFormat("MM/dd/YYYY").format(new Date());

                    System.out.println("Date generated: " + dateToday);


                    if (DBProcedureHelper.borrowBook(
                            CredentialData.getUserLoginID(),
                            Integer.parseInt(model.getValueAt(i,5).toString()),
                            dateToday
                    ) > 0) {
                        NotificationManager.Success("Book Successfully Borrowed.");
                    } else
                        NotificationManager.Error("Book was already Borrowed.");

                } catch (Exception e1) {
                    NotificationManager.Warning("[Borrow Book] " + e1.getMessage());
                }

        }
    }

    private void onBtnReturnBookClick() {
        DefaultTableModel model = (DefaultTableModel) jTable.getModel();

        if (jTable.getSelectedRow() < 0) {
            NotificationManager.Error("Select a row to display");
        } else {
            conn = DBConnection.getConnection();
            int i = jTable.getSelectedRow();
        }


    }

    private BookObject createBookObject() {
        BookObject bookObject = new BookObject();

        String bookTitle = bookTitleJTxt.getText();
        String authorName = authorNameJTxt.getText();
        String pubYear = pubYearJTxt.getText();
        String bookISBM = bookISBMJTxt.getText();
        String bookStatus = bookStatusJTxt.getText();

        bookObject.setTitle(bookTitle);
        bookObject.setAuthor(authorName);
        bookObject.setPubYear(pubYear);
        bookObject.setIsbm(bookISBM);
        bookObject.setStatus(bookStatus);

        return bookObject;
    }
}
