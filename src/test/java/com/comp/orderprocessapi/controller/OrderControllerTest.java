package com.comp.orderprocessapi.controller;

import com.comp.orderprocessapi.models.Order;
import com.comp.orderprocessapi.models.OrderStatus;
import com.comp.orderprocessapi.models.PlaceOrderResponse;
import com.comp.orderprocessapi.repository.MongoRepository;
import com.comp.orderprocessapi.services.OrderProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class OrderControllerTest {

    private MockMvc mvc;

    @InjectMocks
    OrderController orderController;

    @Mock
    MongoRepository mongoRepository;

    @Mock
    OrderProducerService orderProducerService;

    @Mock
    ObjectMapper objectMapper;

    ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() throws Exception {
        JacksonTester.initFields(this, new ObjectMapper());
        mvc = MockMvcBuilders.standaloneSetup(orderController)
                .build();
    }

    @Test
    public void getOrderStatus() throws Exception{
        //given
        Order order = new Order();
        order.setOrderId("525");
        order.setStatus(OrderStatus.PLACED);
        ObjectNode jsonNodes = mapper.createObjectNode();
        jsonNodes.put("orderId", order.getOrderId());
        jsonNodes.put("status", String.valueOf(order.getStatus()));
        BDDMockito.given(mongoRepository.findById("525")).willReturn(java.util.Optional.of(order));
        BDDMockito.given(objectMapper.createObjectNode()).willReturn(mapper.createObjectNode());

        //when
        MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.get("/orders/order/status/525"))
                .andReturn().getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString().equals(mapper.writeValueAsString(jsonNodes)));
    }

    @Test
    public void getOrderStatusForOrderWhichIsNotFound() throws Exception{

        //given
        BDDMockito.given(mongoRepository.findById("45")).willReturn(java.util.Optional.empty());

        //when
        MockHttpServletResponse response = mvc.perform(MockMvcRequestBuilders.get("/orders/order/status/45"))
                .andReturn().getResponse();

        //then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void placeOrder() throws Exception{
        //given
        String order = "{\n" +
                "    \"customerUserName\": \"nanda@gmail.com\",\n" +
                "    \"orderDate\":\"07-Aug-2021\",\n" +
                "    \"paymentMode\": \"UPI\",\n" +
                "    \"shippingAddress\": \"No. 153/11b, solai alagu puram,Madurai - 625 011\",\n" +
                "    \"orderedItems\":[\n" +
                "    { \n" +
                "      \"quantity\" : 1,\n" +
                "      \"productId\" : 178,\n" +
                "      \"productName\" : \"Handwash\",\n" +
                "      \"productPrice\" : 500\n" +
                "      \n" +
                "    },\n" +
                "    { \n" +
                "      \"quantity\" : 2,\n" +
                "      \"productId\" : 192,\n" +
                "      \"productName\" : \"Organic Shampoo\",\n" +
                "      \"productPrice\" : 100\n" +
                "      \n" +
                "    }\n" +
                "    ],\n" +
                "    \"itemsTotalPrice\": 600,\n" +
                "    \"packingCost\": 100,\n" +
                "    \"tax\": 30,\n" +
                "    \"total\":730\n" +
                "}";
        PlaceOrderResponse placeOrderResponse = new PlaceOrderResponse();
        placeOrderResponse.setStatus(OrderStatus.PLACED);
        BDDMockito.given(orderProducerService.publishOrder(any())).willReturn(placeOrderResponse);

        //when & then
        mvc.perform(MockMvcRequestBuilders.post("/orders/order")
                        .accept(MediaType.ALL)
                        .content(order)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated());
    }


    @Test
    public void placeOrderFailed() throws Exception{
        //given
        String order = "{\n" +
                "    \"customerUserName\": \"karthik@gmail.com\",\n" +
                "    \"orderDate\":\"08-Aug-2021\",\n" +
                "    \"paymentMode\": \"UPI\",\n" +
                "    \"shippingAddress\": \"No. 153/11b, solai alagu puram,Madurai - 625 011\",\n" +
                "    \"orderedItems\":[\n" +
                "    { \n" +
                "      \"quantity\" : 1,\n" +
                "      \"productId\" : 79,\n" +
                "      \"productName\" : \"lipstick\",\n" +
                "      \"productPrice\" : 400\n" +
                "      \n" +
                "    },\n" +
                "    { \n" +
                "      \"quantity\" : 2,\n" +
                "      \"productId\" : 90,\n" +
                "      \"productName\" : \"powder\",\n" +
                "      \"productPrice\" : 700\n" +
                "      \n" +
                "    }\n" +
                "    ],\n" +
                "    \"itemsTotalPrice\": 1100,\n" +
                "    \"packingCost\": 100,\n" +
                "    \"tax\": 30,\n" +
                "    \"total\":1230\n" +
                "}";
        PlaceOrderResponse placeOrderResponse = new PlaceOrderResponse();
        placeOrderResponse.setStatus(OrderStatus.FAILED);
        BDDMockito.given(orderProducerService.publishOrder(any())).willReturn(placeOrderResponse);

        //when & then
        mvc.perform(MockMvcRequestBuilders.post("/orders/order")
                        .accept(MediaType.ALL)
                        .content(order)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void placeOrderWithBadRequest() throws Exception{
        //given
        String order = "{\n" +
                "    \"customerUserName\": \"yamuna@gmail.com\",\n" +
                "    \"orderDate\":\"09-Aug-2021\",\n" +
                "    \"paymentMode\": \"UPI\",\n" +
                "    \"orderedItems\":[\n" +
                "    { \n" +
                "      \"quantity\" : 1,\n" +
                "      \"productId\" : 156,\n" +
                "      \"productName\" : \"Organic Handwash\",\n" +
                "      \"productPrice\" : 400\n" +
                "      \n" +
                "    },\n" +
                "    { \n" +
                "      \"quantity\" : 2,\n" +
                "      \"productId\" : 137,\n" +
                "      \"productName\" : \"Organic Shampoo\",\n" +
                "      \"productPrice\" : 500\n" +
                "      \n" +
                "    }\n" +
                "    ],\n" +
                "    \"itemsTotalPrice\": 1400,\n" +
                "    \"packingCost\": 100,\n" +
                "    \"tax\": 30,\n" +
                "    \"total\":1530\n" +
                "}";
        Order orderObject = mapper.readValue(order, Order.class);
        PlaceOrderResponse placeOrderResponse = new PlaceOrderResponse();
        placeOrderResponse.setStatus(OrderStatus.FAILED);
        BDDMockito.given(orderProducerService.publishOrder(orderObject)).willReturn(placeOrderResponse);

        //when & then
        mvc.perform(MockMvcRequestBuilders.post("/orders/order")
                        .accept(MediaType.ALL)
                        .content(order)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }
}
