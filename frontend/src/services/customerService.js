import axios from 'axios';

const API_BASE = 'http://localhost:8080';

export const createCustomer = async (firstName, lastName, email) => {
    const response = await axios.post(`${API_BASE}/customers`, {
        firstName,
        lastName,
        email
    });
    return response.data;
};

export const getAllCustomers = async () => {
    const response = await axios.get(`${API_BASE}/customers`);
    return response.data;
};
