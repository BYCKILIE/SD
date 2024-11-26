import DevicesList from "../device/DevicesList.jsx";
import UsersList from "../user/UsersList.jsx";
import AdminUserDevicesList from "../adminUserDevices/AdminUserDevicesList.jsx";
import MapList from "../MapList/MapList.jsx";
import UserEdit from "../UserEdit/UserEdit.jsx";
import AdminEdit from "../AdminEdit/AdminEdit.jsx";
import AdminDevicesList from "../AdminDevices/AdminDevicesList.jsx";
import DeviceEdit from "../DeviceEdit/DeviceEdit.jsx";
import DeviceCreate from "../DeviceCreate/DeviceCreate.jsx"; // Import CSS for styling

// eslint-disable-next-line react/prop-types
const InfoType = ({ index, mode }) => {
    const components = {
        client: {
            0: <DevicesList />,
            1: null, // monitoring // later
            2: null, // chat // later
            3: <UserEdit />,
        },
        admin: {
            0: <UsersList />,
            1: <AdminDevicesList />,
            2: <DeviceCreate />,
            3: null, // monitoring // later
            4: null, // chat // later
        },
        adminUser: {
            0: <AdminUserDevicesList />,
            1: <MapList />,
            2: <AdminEdit />,
        },
        adminDevice: {
            0: <DeviceEdit />
        },
    };
    return components[mode]?.[index] || null;
};

export default InfoType;