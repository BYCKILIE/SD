import {useEffect, useState} from 'react';
import useAuth from '../hooks/useAuth.js';
import {useNavigate} from "react-router-dom";
import "../styles/home.css";
import MenuForm from "../forms/MenuForm.jsx";
import {useQuery} from "../hooks/useQuery.js";
import useDevices from "../hooks/useDevices.js";

const headers = [
    'Edit'
];

const UserPage = () => {
    const query = useQuery()
    const deviceId = query.get("deviceId");

    const [activeTab, setActiveTab] = useState(0);

    const [names, setNames] = useState({});

    const {adminAuth, admin} = useAuth();
    const {readDevice} = useDevices();
    const navigate = useNavigate();

    useEffect(() => {
        const handleAuth = async () => {
            await adminAuth();
            if (!admin) {
                navigate("/login")
                return
            }
            const data = await readDevice(deviceId);
            setNames({firstName: data.name, lastName: ''});
        }
        handleAuth();
    }, []);

    const changeTabOnClick = async (index) => {
        setActiveTab(index);
    };

    return (
        <MenuForm names={names} headers={headers} activeTab={activeTab} changeTabOnClick={changeTabOnClick} greeting={"Device"}
                  mode={"adminDevice"}/>
    );
};

export default UserPage;
