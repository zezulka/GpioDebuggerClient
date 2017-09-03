package gui.userdata;

import com.thoughtworks.xstream.XStreamException;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import protocol.BoardType;

public class UserDataUtilsTest {

    public UserDataUtilsTest() {
    }

    @Before
    public void init() {
    }

    @After
    public void after() {
        if (TestXmlFiles.MISSING.exists()) {
            TestXmlFiles.MISSING.delete();
        }
    }

    //@Test
    public void emptyFile() {
        assertThatThrownBy(() -> UserDataUtils.initItemsFromFile(TestXmlFiles.EMPTY_FILE)).isInstanceOf(XStreamException.class);
    }

    /**
     * Happy scenario.
     */
    @Test
    public void emptyDevices() {
        assertThat(UserDataUtils.initItemsFromFile(TestXmlFiles.DEVICES_EMPTY)).isEmpty();
    }

    /**
     * Happy scenario.
     */
    @Test
    public void emptySpi() {
        assertThat(UserDataUtils.initItemsFromFile(TestXmlFiles.SPI_EMPTY)).isEmpty();
    }

    /**
     * Happy scenario.
     */
    @Test
    public void emptyI2c() {
        assertThat(UserDataUtils.initItemsFromFile(TestXmlFiles.I2C_EMPTY)).isEmpty();
    }

    @Test
    public void unknownCollection() {
        assertThatThrownBy(() -> UserDataUtils.initItemsFromFile(TestXmlFiles.UNKNOWN)).isInstanceOf(XStreamException.class);
    }

    @Test
    public void invalidFile() {
        assertThatThrownBy(() -> UserDataUtils.initItemsFromFile(TestXmlFiles.INVALID_XML)).isInstanceOf(XStreamException.class);
    }

    /**
     * Happy scenario. When file is missing, this should not raise a throwable,
     * but return empty collection instead.
     */
    @Test
    public void missingFile() {
        assertThat(UserDataUtils.initItemsFromFile(TestXmlFiles.MISSING)).isEmpty();
    }

    /**
     *
     * Happy scenario.
     */
    @Test
    public void addNewDeviceWhenDeviceFileMissing() {
        assertThat(TestXmlFiles.MISSING.exists()).isFalse();
        XStreamListWrapper<String> wrapper = new TestXStreamListWrapper();
        wrapper.addItem("hello");
        UserDataUtils.saveCollectionsToAssociatedFiles(wrapper);
        assertThat(TestXmlFiles.MISSING.exists()).isTrue();
    }

    /**
     *
     * Happy scenario.
     */
    @Test
    public void addNewDeviceWhenDeviceFileMissing2() {
        assertThat(TestXmlFiles.MISSING.exists()).isFalse();
        XStreamListWrapper<String> wrapper = new TestXStreamListWrapper();
        wrapper.addItem("hello");
        wrapper.addItem("world");
        UserDataUtils.saveCollectionsToAssociatedFiles(wrapper);
        assertThat(UserDataUtils.initItemsFromFile(TestXmlFiles.MISSING))
                .containsOnlyElementsOf(wrapper.getItems());
    }

    /**
     * Happy scenario.
     */
    @Test
    public void oneDevice() throws UnknownHostException {
        InetAddress address = InetAddress.getByName("10.42.0.10");
        DeviceValueObject dvo = new DeviceValueObject(address, BoardType.RASPBERRY_PI);
        assertThat(UserDataUtils.initItemsFromFile(TestXmlFiles.ONE_DEVICE))
                .usingFieldByFieldElementComparator().containsOnly(dvo);
    }

    @Test
    public void addItemsToExistingCollection() throws UnknownHostException {
        File referredFile = TestXmlFiles.ONE_DEVICE;
        assertThat(referredFile.exists() && referredFile.canRead() && referredFile.canWrite()).isTrue();
        try {
            XStreamListWrapper<DeviceValueObject> wrapper = new MockedDevices(UserDataUtils.initItemsFromFile(referredFile));

            InetAddress addressExisting = InetAddress.getByName("10.42.0.10");
            DeviceValueObject existing = new DeviceValueObject(addressExisting, BoardType.RASPBERRY_PI);

            InetAddress address = InetAddress.getByName("10.42.0.12");
            DeviceValueObject first = new DeviceValueObject(address, BoardType.RASPBERRY_PI);

            InetAddress addressSecond = InetAddress.getByName("10.42.0.15");
            DeviceValueObject second = new DeviceValueObject(addressSecond, BoardType.CUBIEBOARD);

            wrapper.addItem(first);
            wrapper.addItem(second);

            UserDataUtils.saveCollectionsToAssociatedFiles(wrapper);
            assertThat(UserDataUtils.initItemsFromFile(referredFile))
                    .containsOnly(existing, first, second);
        } finally {
            restoreOneDeviceFile();
        }
    }

    private void restoreOneDeviceFile() {
        InetAddress address;
        try {
            address = InetAddress.getByName("10.42.0.10");
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
        DeviceValueObject dvo = new DeviceValueObject(address, BoardType.RASPBERRY_PI);
        XStreamListWrapper wrapper = new MockedDevices(new ArrayList<>());
        wrapper.addItem(dvo);
        UserDataUtils.saveCollectionsToAssociatedFiles(wrapper);
    }
}
