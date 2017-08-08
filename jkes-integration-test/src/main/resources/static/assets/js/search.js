/**
 * Created by chaokunyang on 2017/8/8.
 */
var search = angular.module('search', [])
    .controller('Admin', ['$scope', '$http', '$interval', function($scope, $http, $interval) {
        $scope.start_all = function () {
            $http.post('/api/search/start_all')
                .then(function (response) {
                    updateProgress();
                });
        };

        $scope.start = function (entityClassName) {
            $http.post('/api/search/start/' + entityClassName)
                .then(function (response) {
                    updateProgress();
                });
        };

        $scope.stop_all = function () {
            $http.put('/api/search/stop_all')
                .then(function (response) {
                    updateProgress();
                });
        };

        $scope.stop = function (entityClassName) {
            $http.put('/api/search/stop/' + entityClassName)
                .then(function (response) {
                    updateProgress();
                });
        };

        var stop;
        $scope.startRefresh = function () {
            // Don't start a new refresh if we are already refreshing
            if(angular.isDefined(stop)) return;
            stop = $interval(updateProgress, 1000);
        };

        $scope.stopRefresh = function () {
            if (angular.isDefined(stop)) {
                $interval.cancel(stop);
                stop = undefined;
            }
        };

        $scope.$on('$destroy', function() {
            // Make sure that the interval is destroyed too
            $scope.stopRefresh();
        });

        updateProgress();
        $scope.startRefresh();

        function updateProgress() {
            $http.get('/api/search/progress').
            then(function(response) {
                $scope.progress = convertJsonToArray(response.data);

                for(var i = 0; i < $scope.progress.length; i++) {
                    if($scope.progress[i].percent !== 100) {
                        $scope.startRefresh();
                        return;
                    }
                }
                $scope.stopRefresh();
            });
        }
    }]);

function convertJsonToArray(json) {
    var arr = [];

    for(var k in json){
        arr.push(json[k])
    }

    return arr;
}