package net.sf.jxls.reader;

import junit.framework.TestCase;
import net.sf.jxls.reader.sample.Department;
import net.sf.jxls.reader.sample.Employee;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Leonid Vysochyn
 */
public class XLSForEachBlockReaderImplTest extends TestCase {
    public static final String dataXLS = "/templates/departmentData.xls";

    public void testRead() throws IOException {
        InputStream inputXLS = new BufferedInputStream(getClass().getResourceAsStream(dataXLS));
        POIFSFileSystem fsInput = new POIFSFileSystem(inputXLS);
        HSSFWorkbook hssfInputWorkbook = new HSSFWorkbook(fsInput);
        HSSFSheet sheet = hssfInputWorkbook.getSheetAt( 0 );
        List mappings = new ArrayList();
        Department department = new Department();
        Map beans = new HashMap();
        beans.put("department", department);
        mappings.add( new BeanCellMapping(7, (short) 0, "employee", "name"));
        mappings.add( new BeanCellMapping(7, (short) 1, "employee", "age"));
        mappings.add( new BeanCellMapping(7, (short) 3, "employee", "payment"));
        mappings.add( new BeanCellMapping(7, (short) 4, "employee", "bonus"));
        XLSBlockReader reader = new SimpleBlockReaderImpl(7, 7, mappings);
        XLSRowCursor cursor = new XLSRowCursorImpl( sheet );
        XLSLoopBlockReader forEachReader = new XLSForEachBlockReaderImpl(7, 7, "department.staff", "employee", Employee.class);
        forEachReader.addBlockReader( reader );
        SectionCheck loopBreakCheck = getLoopBreakCheck();
        forEachReader.setLoopBreakCondition( loopBreakCheck );
        cursor.setCurrentRowNum( 7 );
        forEachReader.read( cursor, beans );
        assertEquals( 4, department.getStaff().size() );
        Employee employee = (Employee) department.getStaff().get(0);
        checkEmployee( employee, "Oleg", new Integer(32), new Double(2000.0), new Double(0.20) );
        employee = (Employee) department.getStaff().get(1);
        checkEmployee( employee, "Yuri", new Integer(29), new Double(1800.0), new Double(0.15) );
        employee = (Employee) department.getStaff().get(2);
        checkEmployee( employee, "Leonid", new Integer(30), new Double(1700.0), new Double(0.20) );
        employee = (Employee) department.getStaff().get(3);
        checkEmployee( employee, "Alex", new Integer(28), new Double(1600.0), new Double(0.20) );
    }

    public void testRead2() throws IOException {
        InputStream inputXLS = new BufferedInputStream(getClass().getResourceAsStream(dataXLS));
        POIFSFileSystem fsInput = new POIFSFileSystem(inputXLS);
        HSSFWorkbook hssfInputWorkbook = new HSSFWorkbook(fsInput);
        HSSFSheet sheet = hssfInputWorkbook.getSheetAt( 2 );
        Department department;
        Map beans = new HashMap();
        List departments = new ArrayList();
        beans.put("departments", departments);
        List chiefMappings = new ArrayList();
        chiefMappings.add( new BeanCellMapping(0, (short) 1, "department", "name") );
        chiefMappings.add( new BeanCellMapping(3, (short) 0, "department", "chief.name") );
        chiefMappings.add( new BeanCellMapping(3, (short) 1, "department.chief.age") );
        chiefMappings.add( new BeanCellMapping(3, (short) 2, "department.chief.payment") );
        chiefMappings.add( new BeanCellMapping(3, (short) 3, "department", "chief.bonus") );
        XLSBlockReader chiefReader = new SimpleBlockReaderImpl(0, 6, chiefMappings);

        List employeeMappings = new ArrayList();
        employeeMappings.add( new BeanCellMapping(7, (short) 0, "employee", "name") );
        employeeMappings.add( new BeanCellMapping(7, (short) 1, "employee", "age") );
        employeeMappings.add( new BeanCellMapping(7, (short) 2, "employee", "payment") );
        employeeMappings.add( new BeanCellMapping(7, (short) 3, "employee", "bonus") );
        XLSBlockReader employeeReader = new SimpleBlockReaderImpl(7, 7, employeeMappings);
        XLSLoopBlockReader employeesReader = new XLSForEachBlockReaderImpl(7, 7, "department.staff", "employee", Employee.class);
        employeesReader.addBlockReader( employeeReader );
        SectionCheck loopBreakCheck = getLoopBreakCheck();
        employeesReader.setLoopBreakCondition( loopBreakCheck );
        XLSLoopBlockReader departmentInfoReader = new XLSForEachBlockReaderImpl(0, 8, "departments", "department", Department.class);
        departmentInfoReader.addBlockReader( chiefReader );
        departmentInfoReader.addBlockReader( employeesReader );
        departmentInfoReader.addBlockReader( new SimpleBlockReaderImpl(8, 8, new ArrayList()));
        loopBreakCheck = new SimpleSectionCheck();
        loopBreakCheck.addRowCheck( new OffsetRowCheckImpl(0) );
        loopBreakCheck.addRowCheck( new OffsetRowCheckImpl(1) );
        departmentInfoReader.setLoopBreakCondition( loopBreakCheck );
        
        XLSRowCursor cursor = new XLSRowCursorImpl( sheet );
        cursor.setCurrentRowNum( 0 );

        departmentInfoReader.read( cursor, beans );

        assertEquals( 3, departments.size() );

        department = (Department) departments.get(0);
        checkDepartmentInfo( department, "IT", "Derek", new Integer(35), new Double(3000.0), new Double(0.30) );
        assertEquals( 5, department.getStaff().size() );
        Employee employee = (Employee) department.getStaff().get(0);
        checkEmployee( employee, "Elsa", new Integer(28), new Double(1500.0), new Double(0.15) );
        employee = (Employee) department.getStaff().get(1);
        checkEmployee( employee, "Oleg", new Integer(32), new Double(2300.0), new Double(0.25) );
        employee = (Employee) department.getStaff().get(2);
        checkEmployee( employee, "Neil", new Integer(34), new Double(2500.0), new Double(0.00) );
        employee = (Employee) department.getStaff().get(3);
        checkEmployee( employee, "Maria", new Integer(34), new Double(1700.0), new Double(0.15) );
        employee = (Employee) department.getStaff().get(4);
        checkEmployee( employee, "John", new Integer(35), new Double(2800.0), new Double(0.20) );

        department = (Department) departments.get(1);
        checkDepartmentInfo( department, "HR", "Betsy", new Integer(37), new Double(2200.0), new Double(0.30) );
        assertEquals( 4, department.getStaff().size() );
        employee = (Employee) department.getStaff().get(0);
        checkEmployee( employee, "Olga", new Integer(26), new Double(1400.0), new Double(0.20) );
        employee = (Employee) department.getStaff().get(1);
        checkEmployee( employee, "Helen", new Integer(30), new Double(2100.0), new Double(0.10) );
        employee = (Employee) department.getStaff().get(2);
        checkEmployee( employee, "Keith", new Integer(24), new Double(1800.0), new Double(0.15) );
        employee = (Employee) department.getStaff().get(3);
        checkEmployee( employee, "Cat", new Integer(34), new Double(1900.0), new Double(0.15) );

        department = (Department) departments.get(2);
        checkDepartmentInfo( department, "BA", "Wendy", new Integer(35), new Double(2900.0), new Double(0.35) );
        assertEquals( 4, department.getStaff().size() );
        employee = (Employee) department.getStaff().get(0);
        checkEmployee( employee, "Denise", new Integer(30), new Double(2400.0), new Double(0.20) );
        employee = (Employee) department.getStaff().get(1);
        checkEmployee( employee, "LeAnn", new Integer(32), new Double(2200.0), new Double(0.15) );
        employee = (Employee) department.getStaff().get(2);
        checkEmployee( employee, "Natali", new Integer(28), new Double(2600.0), new Double(0.10) );
        employee = (Employee) department.getStaff().get(3);
        checkEmployee( employee, "Martha", new Integer(33), new Double(2150.0), new Double(0.25) );


    }

    private void checkDepartmentInfo(Department department, String name, String chiefName, Integer chiefAge, Double chiefPayment, Double chiefBonus){
        assertNotNull( department );
        assertEquals( name, department.getName() );
        checkEmployee( department.getChief(), chiefName, chiefAge, chiefPayment, chiefBonus );
    }

    private void checkEmployee(Employee employee, String name, Integer age, Double payment, Double bonus){
        assertNotNull( employee );
        assertEquals( name, employee.getName() );
        assertEquals( age, employee.getAge() );
        assertEquals( payment, employee.getPayment() );
        assertEquals( bonus, employee.getBonus() );
    }

    private SectionCheck getLoopBreakCheck() {
        OffsetRowCheck rowCheck = new OffsetRowCheckImpl( 0 );
        rowCheck.addCellCheck( new OffsetCellCheckImpl((short) 0, "Employee Payment Totals:") );
        SectionCheck sectionCheck = new SimpleSectionCheck();
        sectionCheck.addRowCheck( rowCheck );
        return sectionCheck;
    }

}
