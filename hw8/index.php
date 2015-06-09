<?php
error_reporting(0);
date_default_timezone_set('America/Los_Angeles');
header('Access-Control-Allow-Origin: *');

new RealEstate();

class RealEstate{

    private $input_data = array();

    function __construct(){
        $xml = '';
        $this->inputCollect();
        echo $this->getResultJSON(); 
        
    }
    
    function inputCollect(){

        if(!empty($_GET)){
            foreach($_GET as $id => $v){
                $this->input_data[$id] = $v;
            }
        }
        if(!empty($_POST)){
            foreach($_POST as $id => $v){
                $this->input_data[$id] = $v;
            }
        }
    }

    function getResultJSON(){
        $query = array(
            'zws-id' => 'X1-ZWz1dxqn5v8yrv_1y8f9', 
            'address' => $this->input_data['streetInput'],
            'citystatezip' => $this->input_data['cityInput'].', '.$this->input_data['stateInput'],
            'rentzestimate' => true
        );


        $url = "http://www.zillow.com/webservice/GetDeepSearchResults.htm?".http_build_query($query);
        //$url = "http://www.zillow.com/webservice/GetDeepSearchResults.htm?zws-id=X1-ZWz1dxqn5v8yrv_1y8f9&address=1248-w-adams-blvd-apt-101&citystatezip=LA%2C+CA&rentzestimate=1";
        //$url = "http://www.zillow.com/webservice/GetDeepSearchResults.htm?zws-id=X1-ZWz1dxqn5v8yrv_1y8f9&address=1249+W+36th+Street&citystatezip=LA%2C+CA&rentzestimate=1";
        $basic_info = simplexml_load_file($url);

        if($basic_info->message->code != 0){
            die("0");
        }
        $res = $basic_info->response->results->result;
        $zestimate = $res->zestimate;
        $rentzestimate = $res->rentzestimate;
        $address = $res->address->street.', '
            .$res->address->city.', '
            .$res->address->state.'-'
            .$res->address->zipcode;
        $link = $res->links->homedetails;

        
        $query = array(
            'zws-id' => 'X1-ZWz1dxqn5v8yrv_1y8f9', 
            'unit-type' => 'percent',
            'zpid' => (string)$res->zpid,
            'width' => '600',
            'height' => '300'
        );
        foreach(array('1year','5years','10years') as $y){
            $query['chartDuration'] = $y;
            $url = "http://www.zillow.com/webservice/GetChart.htm?".http_build_query($query);
            $chart[$y] = simplexml_load_file($url);
            $chart_url[$y] = $chart[$y]->response->url;
        }

        return json_encode(array(
            'result' => array(
                'homedetails' => (string)$link,
                'street' => (string)$res->address->street,
                'city' => (string)$res->address->city,
                'state' => (string)$res->address->state,
                'zipcode' =>  (string)$res->address->zipcode,
                'latitude' => (string)$res->address->latitude,
                'longitude' => (string)$res->address->longitude,
                'useCode' => (string)$res->useCode,
                'lastSoldPrice' => $this->moneyFormat($res->lastSoldPrice),
                'yearBuilt' => (string)$res->yearBuilt,
                'lastSoldDate' => $this->_date($res->lastSoldDate),
                'lotSizeSqFt' => $this->areaFormat($res->lotSizeSqFt),
                'estimateLastUpdate' => $this->_date($zestimate->{'last-updated'}),
                'estimateAmount' => $this->moneyFormat($zestimate->amount),
                'finishedSqFt' => $this->areaFormat($res->finishedSqFt),
                'estimateValueChangeSign' => $this->change30Format($zestimate->valueChange,true),
                'imgn' => 'http://www-scf.usc.edu/~csci571/2014Spring/hw6/down_r.gif',
                'imgp' => 'http://www-scf.usc.edu/~csci571/2014Spring/hw6/up_g.gif',
                'estimateValueChange' => $this->change30Format($zestimate->valueChange),
                'bathrooms' => (string)$res->bathrooms,
                'estimateValuationRangeLow' => $this->moneyFormat($zestimate->valuationRange->low),
                'estimateValuationRangeHigh' => $this->moneyFormat($zestimate->valuationRange->high),
                'bedrooms' => (string)$res->bedrooms,
                'restimateLastUpdate' => $this->_date($rentzestimate->{'last-updated'}),
                'restimateAmount' => $this->moneyFormat($rentzestimate->amount),
                'taxAssessmentYear' => (string)$res->taxAssessmentYear,
                'restimateValueChangeSign' => $this->change30Format($rentzestimate->valueChange,true),
                'restimtaeValueChange' => $this->change30Format($rentzestimate->valueChange),
                'taxAssessment' => $this->moneyFormat($res->taxAssessment),
                'restimateValuationRangeLow' => $this->moneyFormat($rentzestimate->valuationRange->low),
                'restimateValuationRangeHigh' => $this->moneyFormat($rentzestimate->valuationRange->high)
            ),
            'chart' => array(
                '1year' => array(
                    'url' => (string)$chart_url['1year'],
                ),
                '5years' => array(
                    'url' => (string)$chart_url['5years'],
                ),
                '10years' => array(
                    'url' => (string)$chart_url['10years']
                )
            )
        )); 
         
    }

    function moneyFormat($str){
        $str = trim($str);
        if(empty($str)){
            return '';
        }
        return number_format((double)$str,2,'.',',');
    }
    function areaFormat($str){
        $str = trim($str);
        if(empty($str)){
            return '';
        }
        return number_format((double)$str)." sq.ft.";
    }
    function _date($str){
        $str = trim($str);
        if(empty($str)){
            return '';
        }
        return date('d-M-Y',strtotime($str));
    }
    function change30Format($str,$imgf=0){
        $str = trim($str);
        if(empty($str)){
            return '';
        }
        $num = (double)$str;
        if($imgf){
            if($num <0){
                return '-';
            }elseif($num > 0){
                return '+';
            }else{
                return '';
            }
        }else{
            return $this->moneyFormat(abs($num));
        }
    }
}


?>
