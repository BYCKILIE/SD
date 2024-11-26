import {useEffect, useState} from 'react';
import useAuth from '../hooks/useAuth.js';
import {useNavigate} from "react-router-dom";
import "../styles/home.css";
import MenuForm from "../forms/MenuForm.jsx";
import useProfiles from "../hooks/userProfiles.js";

const headers = [
    'Devices',
    'Monitoring',
    'Chat',
    'Edit'
];

const HomePage = () => {
    const [activeTab, setActiveTab] = useState(0);

    const [names, setNames] = useState("");

    const {clientAuth, client} = useAuth();
    const {readProfile} = useProfiles();
    const navigate = useNavigate();

    useEffect(() => {
        const handleAuth = async () => {
            await clientAuth()
            if (!client) {
                navigate("/login")
                return
            }
            const names = await readProfile()
            setNames(names)
        }
        handleAuth()
    }, []);

    const changeTabOnClick = async (index) => {
        setActiveTab(index);
    };

    return (
        <MenuForm names={names} headers={headers} activeTab={activeTab} changeTabOnClick={changeTabOnClick}
             mode={"client"}/>
    );
};

export default HomePage;
